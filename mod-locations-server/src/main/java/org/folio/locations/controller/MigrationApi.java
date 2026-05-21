package org.folio.locations.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/migrate")
public interface MigrationApi {

  @PostMapping("/institutions")
  ResponseEntity<Void> migrateInstitutions(@RequestBody List<@Valid Institution> institutions);

  @PostMapping("/campuses")
  ResponseEntity<Void> migrateCampuses(@RequestBody List<@Valid Campus> campuses);

  @PostMapping("/libraries")
  ResponseEntity<Void> migrateLibraries(@RequestBody List<@Valid Library> libraries);

  @PostMapping("/locations")
  ResponseEntity<Void> migrateLocations(@RequestBody List<@Valid Location> locations);

  @PostMapping("/service-points")
  ResponseEntity<Void> migrateServicePoints(@RequestBody List<@Valid ServicePoint> servicePoints);

  @PostMapping("/service-points-users")
  ResponseEntity<Void> migrateServicePointsUsers(@RequestBody List<@Valid ServicePointsUser> servicePointsUsers);
}
