package org.folio.locations.service.migration.impl;

import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.mapper.ServicePointUserMapper;
import org.folio.locations.repository.ServicePointUserRepository;
import org.folio.locations.service.migration.AbstractMigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ServicePointUserMigrationService
  extends AbstractMigrationService<ServicePointsUser, ServicePointUserEntity> {

  public ServicePointUserMigrationService(ServicePointUserRepository repository, ServicePointUserMapper mapper) {
    super(repository, mapper);
  }

  @Override
  protected @Nullable Metadata extractMetadata(ServicePointsUser dto) {
    return dto.getMetadata();
  }
}
