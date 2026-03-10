package org.folio.locations.service.migration.impl;

import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipId;
import org.folio.locations.mapper.ServicePointMapper;
import org.folio.locations.repository.ServicePointRepository;
import org.folio.locations.service.migration.AbstractMigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ServicePointMigrationService extends AbstractMigrationService<ServicePoint, ServicePointEntity> {

  public ServicePointMigrationService(ServicePointRepository repository, ServicePointMapper mapper) {
    super(repository, mapper);
  }

  @Override
  protected @Nullable Metadata extractMetadata(ServicePoint dto) {
    return dto.getMetadata();
  }

  @Override
  protected void beforeMigrate(ServicePointEntity entity) {
    entity.getStaffSlips().forEach(slip -> {
      if (slip.getId() == null) {
        slip.setId(new ServicePointStaffSlipId());
      }
      slip.getId().setServicePointId(entity.getId());
      slip.setServicePoint(entity);
    });
  }
}
