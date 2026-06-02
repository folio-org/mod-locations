package org.folio.locations.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.CampusesCollection;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.InstitutionsCollection;
import org.folio.locations.domain.dto.LibrariesCollection;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.LocationsCollection;
import org.folio.locations.rest.resource.LocationsApi;
import org.folio.locations.service.crud.CampusService;
import org.folio.locations.service.crud.InstitutionService;
import org.folio.locations.service.crud.LibraryService;
import org.folio.locations.service.crud.LocationService;
import org.folio.locations.service.crud.ShadowFilterContext;
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
  private final InstitutionService institutionService;
  private final CampusService campusService;
  private final LibraryService libraryService;

  @Override
  public ResponseEntity<Location> createLocation(Location location) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(location));
  }

  @Override
  public ResponseEntity<Campus> createLocationCampus(Campus campus) {
    return ResponseEntity.status(HttpStatus.CREATED).body(campusService.create(campus));
  }

  @Override
  public ResponseEntity<Institution> createLocationInstitution(Institution institution) {
    return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.create(institution));
  }

  @Override
  public ResponseEntity<Library> createLocationLibrary(Library library) {
    return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.create(library));
  }

  @Override
  public ResponseEntity<Void> deleteLocationById(UUID id) {
    service.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocationCampusById(UUID id) {
    campusService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocationCampuses() {
    campusService.deleteAll();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocationInstitutionById(UUID id) {
    institutionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocationInstitutions() {
    institutionService.deleteAll();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocationLibraries() {
    libraryService.deleteAll();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteLocationLibraryById(UUID id) {
    libraryService.deleteById(id);
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
  public ResponseEntity<Campus> getLocationCampusById(UUID id) {
    return ResponseEntity.ok(campusService.getById(id));
  }

  @Override
  public ResponseEntity<CampusesCollection> getLocationCampuses(@Nullable String query, Integer limit, Integer offset,
                                                                Boolean includeShadow) {
    return ResponseEntity.ok(campusService.getAll(new ShadowFilterContext(query, limit, offset, includeShadow)));
  }

  @Override
  public ResponseEntity<Institution> getLocationInstitutionById(UUID id) {
    return ResponseEntity.ok(institutionService.getById(id));
  }

  @Override
  public ResponseEntity<InstitutionsCollection> getLocationInstitutions(@Nullable String query, Integer limit,
                                                                        Integer offset, Boolean includeShadow) {
    return ResponseEntity.ok(institutionService.getAll(new ShadowFilterContext(query, limit, offset, includeShadow)));
  }

  @Override
  public ResponseEntity<LibrariesCollection> getLocationLibraries(@Nullable String query, Integer limit,
                                                                  Integer offset, Boolean includeShadow) {
    return ResponseEntity.ok(libraryService.getAll(new ShadowFilterContext(query, limit, offset, includeShadow)));
  }

  @Override
  public ResponseEntity<Library> getLocationLibraryById(UUID id) {
    return ResponseEntity.ok(libraryService.getById(id));
  }

  @Override
  public ResponseEntity<LocationsCollection> getLocations(@Nullable String query, Integer limit, Integer offset,
                                                          Boolean includeShadow) {
    return ResponseEntity.ok(service.getAll(new ShadowFilterContext(query, limit, offset, includeShadow)));
  }

  @Override
  public ResponseEntity<Void> updateLocationById(UUID id, Location location) {
    service.update(id, location);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> updateLocationCampusById(UUID id, Campus campus) {
    campusService.update(id, campus);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> updateLocationInstitutionById(UUID id, Institution institution) {
    institutionService.update(id, institution);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> updateLocationLibraryById(UUID id, Library library) {
    libraryService.update(id, library);
    return ResponseEntity.noContent().build();
  }
}
