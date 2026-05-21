package org.folio.locations.service.migration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipEntity;
import org.folio.locations.mapper.ServicePointMapper;
import org.folio.locations.repository.ServicePointRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ServicePointMigrationServiceTest {

  private static final UUID SERVICE_POINT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

  @Mock
  private ServicePointRepository repository;
  @Mock
  private ServicePointMapper mapper;

  @Test
  void migrate_positive_staffSlipIdsAreSynced() {
    var dto = new ServicePoint().id(SERVICE_POINT_ID);
    var slipEntity = new ServicePointStaffSlipEntity();
    var spEntity = new ServicePointEntity();
    spEntity.setId(SERVICE_POINT_ID);
    spEntity.setStaffSlips(new ArrayList<>(List.of(slipEntity)));
    when(mapper.toEntity(dto)).thenReturn(spEntity);
    var service = new ServicePointMigrationService(repository, mapper);

    service.migrate(List.of(dto));

    assertThat(slipEntity.getId()).isNotNull();
    assertThat(slipEntity.getId().getServicePointId()).isEqualTo(SERVICE_POINT_ID);
    assertThat(slipEntity.getServicePoint()).isSameAs(spEntity);
  }

  @Test
  void migrate_positive_existingStaffSlipIdPreserved() {
    var staffSlipId = UUID.randomUUID();
    var slipEntity = new ServicePointStaffSlipEntity();
    var existingId = new org.folio.locations.domain.entity.ServicePointStaffSlipId(null, staffSlipId);
    slipEntity.setId(existingId);
    var dto = new ServicePoint().id(SERVICE_POINT_ID);
    var spEntity = new ServicePointEntity();
    spEntity.setId(SERVICE_POINT_ID);
    spEntity.setStaffSlips(new ArrayList<>(List.of(slipEntity)));
    when(mapper.toEntity(dto)).thenReturn(spEntity);
    var service = new ServicePointMigrationService(repository, mapper);

    service.migrate(List.of(dto));

    assertThat(slipEntity.getId().getStaffSlipId()).isEqualTo(staffSlipId);
    assertThat(slipEntity.getId().getServicePointId()).isEqualTo(SERVICE_POINT_ID);
  }

  @Test
  void migrate_positive_emptyStaffSlips_noError() {
    var dto = new ServicePoint().id(SERVICE_POINT_ID);
    var spEntity = new ServicePointEntity();
    spEntity.setId(SERVICE_POINT_ID);
    when(mapper.toEntity(dto)).thenReturn(spEntity);
    var service = new ServicePointMigrationService(repository, mapper);

    service.migrate(List.of(dto));

    assertThat(spEntity.getStaffSlips()).isEmpty();
  }
}
