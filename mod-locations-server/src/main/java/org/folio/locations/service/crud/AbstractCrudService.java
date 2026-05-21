package org.folio.locations.service.crud;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.entity.AbstractEntity;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.domain.event.DomainEventType;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.mapper.EntityMapper;
import org.folio.locations.service.event.DomainEventPublisher;
import org.folio.locations.service.validator.DtoValidator;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.cql.JpaCqlRepository;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;

@NullMarked
public abstract class AbstractCrudService<D, C, E extends AbstractEntity<UUID>> {

  protected static final String ALL_RECORDS_CQL = "cql.allRecords=1";

  protected final JpaCqlRepository<E, UUID> repository;
  protected final EntityMapper<D, E> mapper;
  protected final DtoValidator<D> dtoValidator;
  protected final FolioExecutionContext context;
  protected final DomainEventPublisher publisher;

  protected AbstractCrudService(JpaCqlRepository<E, UUID> repository, EntityMapper<D, E> mapper,
                                DtoValidator<D> dtoValidator, FolioExecutionContext context,
                                DomainEventPublisher publisher) {
    this.repository = repository;
    this.mapper = mapper;
    this.dtoValidator = dtoValidator;
    this.context = context;
    this.publisher = publisher;
  }

  @Transactional(readOnly = true)
  public D getById(UUID id) {
    return repository.findById(id)
      .map(mapper::toDto)
      .orElseThrow(() -> notFound(id));
  }

  @Transactional
  public D create(D dto) {
    dtoValidator.validate(dto);
    var entity = mapper.toEntity(dto);
    if (entity.getId() == null) {
      entity.setId(UUID.randomUUID());
    }
    entity.setCreatedDate(OffsetDateTime.now());
    entity.setCreatedByUserId(context.getUserId());
    beforeCreate(dto, entity);
    var saved = repository.save(entity);
    var resultDto = mapper.toDto(saved);
    publisher.publish(buildEvent(DomainEventType.CREATE, saved.getId(), null, resultDto));
    return resultDto;
  }

  @Transactional
  public void update(UUID id, D dto) {
    dtoValidator.validate(dto);
    var entity = repository.findById(id)
      .orElseThrow(() -> notFound(id));
    final var oldDto = mapper.toDto(entity);
    mapper.updateEntity(dto, entity);
    entity.setUpdatedDate(OffsetDateTime.now());
    entity.setUpdatedByUserId(context.getUserId());
    beforeUpdate(dto, entity);
    var saved = repository.save(entity);
    publisher.publish(buildEvent(DomainEventType.UPDATE, id, oldDto, mapper.toDto(saved)));
  }

  @Transactional
  public void deleteById(UUID id) {
    var entity = repository.findById(id)
      .orElseThrow(() -> notFound(id));
    var oldDto = mapper.toDto(entity);
    repository.deleteById(id);
    publisher.publish(buildEvent(DomainEventType.DELETE, id, oldDto, null));
  }

  @Transactional
  public void deleteAll() {
    var entities = repository.findAll();
    var snapshots = entities.stream()
      .map(entity -> buildEvent(DomainEventType.DELETE, entity.getId(), mapper.toDto(entity), null))
      .toList();
    repository.deleteAll(entities);
    snapshots.forEach(publisher::publish);
  }

  protected C getCollection(String cql, Integer limit, Integer offset) {
    var page = repository.findByCql(cql, OffsetRequest.of(offset, limit));
    var dtos = page.getContent().stream().map(mapper::toDto).toList();
    return buildCollection(dtos, (int) page.getTotalElements());
  }

  protected static String buildCql(@Nullable String query, @Nullable Boolean includeShadow) {
    var shadowFilter = Boolean.TRUE.equals(includeShadow) ? null : "isShadow==false";
    if (query != null && shadowFilter != null) {
      return "(" + query + ") AND " + shadowFilter;
    }
    if (query != null) {
      return "(" + query + ")";
    }
    if (shadowFilter != null) {
      return shadowFilter;
    }
    return ALL_RECORDS_CQL;
  }

  protected abstract C buildCollection(List<D> dtos, int totalRecords);

  protected abstract NotFoundException notFound(UUID id);

  protected abstract ResourceType resourceType();

  protected void beforeCreate(D dto, E entity) { }

  protected void beforeUpdate(D dto, E entity) { }

  private DomainEvent<D> buildEvent(DomainEventType type, UUID resourceId,
                                    @Nullable D oldEntity, @Nullable D newEntity) {
    return DomainEvent.<D>builder()
      .eventId(UUID.randomUUID())
      .eventTs(System.currentTimeMillis())
      .resourceType(resourceType())
      .type(type)
      .tenant(context.getTenantId())
      .userId(context.getUserId())
      .resourceId(resourceId)
      .oldResource(oldEntity)
      .newResource(newEntity)
      .build();
  }
}
