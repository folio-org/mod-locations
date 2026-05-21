package org.folio.locations.service.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.AbstractEntity;
import org.folio.locations.mapper.EntityMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@NullMarked
public abstract class AbstractMigrationService<D, E extends AbstractEntity<UUID>> {

  protected final JpaRepository<E, UUID> repository;
  protected final EntityMapper<D, E> mapper;

  protected AbstractMigrationService(JpaRepository<E, UUID> repository, EntityMapper<D, E> mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public void migrate(List<D> dtos) {
    var entities = new ArrayList<E>();
    for (D dto : dtos) {
      E entityWithMetadata = toEntityWithMetadata(dto);
      beforeMigrate(entityWithMetadata);
      entities.add(entityWithMetadata);
    }
    repository.saveAll(entities);
  }

  protected abstract @Nullable Metadata extractMetadata(D dto);

  protected void beforeMigrate(E entity) { }

  private E toEntityWithMetadata(D dto) {
    var entity = mapper.toEntity(dto);
    applyMetadata(entity, extractMetadata(dto));
    return entity;
  }

  private void applyMetadata(E entity, @Nullable Metadata metadata) {
    if (metadata == null) {
      return;
    }
    entity.setCreatedDate(metadata.getCreatedDate());
    entity.setCreatedByUserId(metadata.getCreatedByUserId());
    entity.setUpdatedDate(metadata.getUpdatedDate());
    entity.setUpdatedByUserId(metadata.getUpdatedByUserId());
  }
}
