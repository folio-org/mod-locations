package org.folio.locations.service.impl;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.LibrariesCollection;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.exception.LibraryNotFoundException;
import org.folio.locations.mapper.LibraryMapper;
import org.folio.locations.repository.LibraryRepository;
import org.folio.locations.service.AbstractCrudService;
import org.folio.locations.service.LibraryService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryServiceImpl
  extends AbstractCrudService<Library, LibrariesCollection, LibraryEntity>
  implements LibraryService {

  public LibraryServiceImpl(LibraryRepository repository, LibraryMapper mapper,
                            FolioExecutionContext context) {
    super(repository, mapper, l -> {}, context);
  }

  @Override
  @Transactional(readOnly = true)
  public LibrariesCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow) {
    var cql = buildCql(query, includeShadow);
    return getCollection(cql, limit, offset);
  }

  @Override
  protected LibrariesCollection buildCollection(List<Library> dtos, int totalRecords) {
    return new LibrariesCollection(dtos, totalRecords);
  }

  @Override
  protected NotFoundException notFound(UUID id) {
    return new LibraryNotFoundException(id);
  }
}
