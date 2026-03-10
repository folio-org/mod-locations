package org.folio.locations.service.migration.impl;

import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.mapper.InstitutionMapper;
import org.folio.locations.repository.InstitutionRepository;
import org.folio.locations.service.migration.AbstractMigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InstitutionMigrationService extends AbstractMigrationService<Institution, InstitutionEntity> {

  public InstitutionMigrationService(InstitutionRepository repository, InstitutionMapper mapper) {
    super(repository, mapper);
  }

  @Override
  protected @Nullable Metadata extractMetadata(Institution dto) {
    return dto.getMetadata();
  }
}
