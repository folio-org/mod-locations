package org.folio.locations.service.crud;

import java.util.UUID;
import org.folio.locations.domain.dto.LibrariesCollection;
import org.folio.locations.domain.dto.Library;
import org.jspecify.annotations.Nullable;

public interface LibraryService {

  LibrariesCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow);

  Library getById(UUID id);

  Library create(Library library);

  void update(UUID id, Library library);

  void deleteById(UUID id);

  void deleteAll();
}
