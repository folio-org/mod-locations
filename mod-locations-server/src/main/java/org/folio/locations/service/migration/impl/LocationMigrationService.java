package org.folio.locations.service.migration.impl;

import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.mapper.LocationMapper;
import org.folio.locations.repository.LocationRepository;
import org.folio.locations.service.migration.AbstractMigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class LocationMigrationService extends AbstractMigrationService<Location, LocationEntity> {

  public LocationMigrationService(LocationRepository repository, LocationMapper mapper) {
    super(repository, mapper);
  }

  @Override
  protected @Nullable Metadata extractMetadata(Location dto) {
    return dto.getMetadata();
  }
}
