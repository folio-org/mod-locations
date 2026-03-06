package org.folio.locations.service;

import java.util.UUID;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.LocationsCollection;
import org.jspecify.annotations.Nullable;

public interface LocationService {

  LocationsCollection getAll(@Nullable String query, Integer limit, Integer offset, Boolean includeShadow);

  Location getById(UUID id);

  Location create(Location location);

  void update(UUID id, Location location);

  void deleteById(UUID id);

  void deleteAll();
}
