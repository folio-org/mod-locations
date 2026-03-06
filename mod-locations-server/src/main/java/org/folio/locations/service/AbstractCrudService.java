package org.folio.locations.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.entity.AbstractEntity;
import org.folio.locations.mapper.EntityMapper;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.cql.JpaCqlRepository;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.NullMarked;
import org.springframework.transaction.annotation.Transactional;

@NullMarked
public abstract class AbstractCrudService<D, C, E extends AbstractEntity<UUID>> {

  protected static final String ALL_RECORDS_CQL = "cql.allRecords=1";

  protected final JpaCqlRepository<E, UUID> repository;
  protected final EntityMapper<D, E> mapper;
  protected final FolioExecutionContext context;

  protected AbstractCrudService(JpaCqlRepository<E, UUID> repository, EntityMapper<D, E> mapper,
                                FolioExecutionContext context) {
    this.repository = repository;
    this.mapper = mapper;
    this.context = context;
  }

  @Transactional(readOnly = true)
  public D getById(UUID id) {
    return repository.findById(id)
      .map(mapper::toDto)
      .orElseThrow(() -> notFound(id));
  }

  @Transactional
  public D create(D dto) {
    var entity = mapper.toEntity(dto);
    if (entity.getId() == null) {
      entity.setId(UUID.randomUUID());
    }
    entity.setCreatedDate(OffsetDateTime.now());
    entity.setCreatedByUserId(context.getUserId());
    beforeCreate(dto, entity);
    return mapper.toDto(repository.save(entity));
  }

  @Transactional
  public void update(UUID id, D dto) {
    var entity = repository.findById(id)
      .orElseThrow(() -> notFound(id));
    mapper.updateEntity(dto, entity);
    entity.setUpdatedDate(OffsetDateTime.now());
    entity.setUpdatedByUserId(context.getUserId());
    beforeUpdate(dto, entity);
    repository.save(entity);
  }

  @Transactional
  public void deleteById(UUID id) {
    if (!repository.existsById(id)) {
      throw notFound(id);
    }
    repository.deleteById(id);
  }

  @Transactional
  public void deleteAll() {
    repository.deleteAll();
  }

  protected C getCollection(String cql, Integer limit, Integer offset) {
    var page = repository.findByCql(cql, OffsetRequest.of(offset, limit));
    var dtos = page.getContent().stream().map(mapper::toDto).toList();
    return buildCollection(dtos, (int) page.getTotalElements());
  }

  protected abstract C buildCollection(List<D> dtos, int totalRecords);

  protected abstract NotFoundException notFound(UUID id);

  protected void beforeCreate(D dto, E entity) { }

  protected void beforeUpdate(D dto, E entity) { }
}
