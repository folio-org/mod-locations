package org.folio.locations.service.migration.impl;

import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.mapper.CampusMapper;
import org.folio.locations.repository.CampusRepository;
import org.folio.locations.service.migration.AbstractMigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class CampusMigrationService extends AbstractMigrationService<Campus, CampusEntity> {

  public CampusMigrationService(CampusRepository repository, CampusMapper mapper) {
    super(repository, mapper);
  }

  @Override
  protected @Nullable Metadata extractMetadata(Campus dto) {
    return dto.getMetadata();
  }
}
