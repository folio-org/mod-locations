package org.folio.locations.service;

import java.util.UUID;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.InstitutionsCollection;
import org.jspecify.annotations.Nullable;

public interface InstitutionService {

  InstitutionsCollection getAll(@Nullable String query, Integer limit, Integer offset);

  Institution getById(UUID id);

  Institution create(Institution institution);

  void update(UUID id, Institution institution);

  void deleteById(UUID id);

  void deleteAll();
}
