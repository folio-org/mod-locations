package org.folio.locations.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.service.migration.impl.CampusMigrationService;
import org.folio.locations.service.migration.impl.InstitutionMigrationService;
import org.folio.locations.service.migration.impl.LibraryMigrationService;
import org.folio.locations.service.migration.impl.LocationMigrationService;
import org.folio.locations.service.migration.impl.ServicePointMigrationService;
import org.folio.locations.service.migration.impl.ServicePointUserMigrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class MigrationController implements MigrationApi {

  private final InstitutionMigrationService institutionMigrationService;
  private final CampusMigrationService campusMigrationService;
  private final LibraryMigrationService libraryMigrationService;
  private final LocationMigrationService locationMigrationService;
  private final ServicePointMigrationService servicePointMigrationService;
  private final ServicePointUserMigrationService servicePointUserMigrationService;

  @Override
  public ResponseEntity<Void> migrateInstitutions(List<Institution> institutions) {
    institutionMigrationService.migrate(institutions);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> migrateCampuses(List<Campus> campuses) {
    campusMigrationService.migrate(campuses);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> migrateLibraries(List<Library> libraries) {
    libraryMigrationService.migrate(libraries);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> migrateLocations(List<Location> locations) {
    locationMigrationService.migrate(locations);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> migrateServicePoints(List<ServicePoint> servicePoints) {
    servicePointMigrationService.migrate(servicePoints);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> migrateServicePointsUsers(List<ServicePointsUser> servicePointsUsers) {
    servicePointUserMigrationService.migrate(servicePointsUsers);
    return ResponseEntity.noContent().build();
  }
}
