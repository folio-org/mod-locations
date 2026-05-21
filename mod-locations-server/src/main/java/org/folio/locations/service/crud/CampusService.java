package org.folio.locations.service.crud;

import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.CampusesCollection;
import org.jspecify.annotations.Nullable;

public interface CampusService {

  CampusesCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow);

  Campus getById(UUID id);

  Campus create(Campus campus);

  void update(UUID id, Campus campus);

  void deleteById(UUID id);

  void deleteAll();
}
