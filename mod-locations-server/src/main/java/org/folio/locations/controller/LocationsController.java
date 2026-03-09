package org.folio.locations.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.LocationsCollection;
import org.folio.locations.rest.resource.LocationsApi;
import org.folio.locations.service.crud.LocationService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class LocationsController implements LocationsApi {

  private final LocationService service;

  @Override
  public ResponseEntity<Location> createLocation(Location location) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(location));
  }

  @Override
  public ResponseEntity<Void> deleteLocationById(UUID id) {
    service.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocations() {
    service.deleteAll();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Location> getLocationById(UUID id) {
    return ResponseEntity.ok(service.getById(id));
  }

  @Override
  public ResponseEntity<LocationsCollection> getLocations(@Nullable String query, Integer limit, Integer offset,
                                                          Boolean includeShadow) {
    return ResponseEntity.ok(service.getAll(query, limit, offset, includeShadow));
  }

  @Override
  public ResponseEntity<Void> updateLocationById(UUID id, Location location) {
    service.update(id, location);
    return ResponseEntity.noContent().build();
  }
}
