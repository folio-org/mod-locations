package org.folio.locations.service.migration.impl;

import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.mapper.LibraryMapper;
import org.folio.locations.repository.LibraryRepository;
import org.folio.locations.service.migration.AbstractMigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class LibraryMigrationService extends AbstractMigrationService<Library, LibraryEntity> {

  public LibraryMigrationService(LibraryRepository repository, LibraryMapper mapper) {
    super(repository, mapper);
  }

  @Override
  protected @Nullable Metadata extractMetadata(Library dto) {
    return dto.getMetadata();
  }
}
