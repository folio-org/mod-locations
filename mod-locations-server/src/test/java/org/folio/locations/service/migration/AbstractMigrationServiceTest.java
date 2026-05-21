package org.folio.locations.service.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.mapper.InstitutionMapper;
import org.folio.locations.repository.InstitutionRepository;
import org.folio.locations.service.migration.impl.InstitutionMigrationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AbstractMigrationServiceTest {

  private static final UUID ENTITY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final OffsetDateTime CREATED_DATE = OffsetDateTime.parse("2023-01-01T00:00:00Z");
  private static final OffsetDateTime UPDATED_DATE = OffsetDateTime.parse("2023-06-01T00:00:00Z");

  @Mock
  private InstitutionRepository repository;
  @Mock
  private InstitutionMapper mapper;

  // ── migrate: metadata wiring ──────────────────────────────────────────────

  @Test
  void migrate_positive_metadataPreservedOnEntity() {
    var service = new InstitutionMigrationService(repository, mapper);
    var metadata = buildMetadata();
    var dto = new Institution("Main", "MAIN").id(ENTITY_ID).metadata(metadata);
    var entity = entityWithId(ENTITY_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);

    service.migrate(List.of(dto));

    assertThat(entity.getCreatedDate()).isEqualTo(CREATED_DATE);
    assertThat(entity.getCreatedByUserId()).isEqualTo(USER_ID);
    assertThat(entity.getUpdatedDate()).isEqualTo(UPDATED_DATE);
    assertThat(entity.getUpdatedByUserId()).isEqualTo(USER_ID);
  }

  @Test
  void migrate_positive_nullMetadataIsHandledGracefully() {
    var service = new InstitutionMigrationService(repository, mapper);
    var dto = new Institution("Main", "MAIN").id(ENTITY_ID);
    var entity = entityWithId(ENTITY_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);

    service.migrate(List.of(dto));

    assertThat(entity.getCreatedDate()).isNull();
    assertThat(entity.getCreatedByUserId()).isNull();
  }

  // ── migrate: ID handling ──────────────────────────────────────────────────

  @Test
  void migrate_positive_idPreservedWhenEntityHasId() {
    var service = new InstitutionMigrationService(repository, mapper);
    var dto = new Institution("Main", "MAIN").id(ENTITY_ID);
    var entity = entityWithId(ENTITY_ID);
    when(mapper.toEntity(dto)).thenReturn(entity);

    service.migrate(List.of(dto));

    assertThat(entity.getId()).isEqualTo(ENTITY_ID);
  }

  // ── migrate: repository interaction ──────────────────────────────────────

  @Test
  @SuppressWarnings("unchecked")
  void migrate_positive_savesAllMappedEntities() {
    var service = new InstitutionMigrationService(repository, mapper);
    var dto1 = new Institution("One", "ONE").id(UUID.randomUUID());
    var dto2 = new Institution("Two", "TWO").id(UUID.randomUUID());
    var entity1 = entityWithId(UUID.randomUUID());
    var entity2 = entityWithId(UUID.randomUUID());
    when(mapper.toEntity(dto1)).thenReturn(entity1);
    when(mapper.toEntity(dto2)).thenReturn(entity2);

    service.migrate(List.of(dto1, dto2));

    var captor = (ArgumentCaptor<List<InstitutionEntity>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
    verify(repository).saveAll(captor.capture());
    assertThat(captor.getValue()).containsExactly(entity1, entity2);
  }

  @Test
  void migrate_positive_emptyListCallsSaveAllWithEmptyList() {
    var service = new InstitutionMigrationService(repository, mapper);

    service.migrate(List.of());

    verify(repository).saveAll(List.of());
    verifyNoInteractions(mapper);
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private InstitutionEntity entityWithId(UUID id) {
    var entity = new InstitutionEntity();
    entity.setId(id);
    return entity;
  }

  private Metadata buildMetadata() {
    return new Metadata()
      .createdDate(CREATED_DATE)
      .createdByUserId(USER_ID)
      .updatedDate(UPDATED_DATE)
      .updatedByUserId(USER_ID);
  }
}
