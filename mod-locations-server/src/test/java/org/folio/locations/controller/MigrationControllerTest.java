package org.folio.locations.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.UUID;
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
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MigrationControllerTest {

  private static final UUID INSTITUTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CAMPUS_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID LIBRARY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID LOCATION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID SERVICE_POINT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID USER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

  @Mock private InstitutionMigrationService institutionMigrationService;
  @Mock private CampusMigrationService campusMigrationService;
  @Mock private LibraryMigrationService libraryMigrationService;
  @Mock private LocationMigrationService locationMigrationService;
  @Mock private ServicePointMigrationService servicePointMigrationService;
  @Mock private ServicePointUserMigrationService servicePointUserMigrationService;

  @InjectMocks
  private MigrationController controller;

  @Test
  void migrateInstitutions_positive_delegatesToServiceAndReturns204() {
    var dtos = List.of(new Institution("Main", "MAIN").id(INSTITUTION_ID));

    var response = controller.migrateInstitutions(dtos);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(institutionMigrationService).migrate(dtos);
    verifyNoMoreInteractions(institutionMigrationService, campusMigrationService, libraryMigrationService,
      locationMigrationService, servicePointMigrationService, servicePointUserMigrationService);
  }

  @Test
  void migrateCampuses_positive_delegatesToServiceAndReturns204() {
    var dtos = List.of(new Campus("City", "C", INSTITUTION_ID).id(CAMPUS_ID));

    var response = controller.migrateCampuses(dtos);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(campusMigrationService).migrate(dtos);
    verifyNoMoreInteractions(institutionMigrationService, campusMigrationService, libraryMigrationService,
      locationMigrationService, servicePointMigrationService, servicePointUserMigrationService);
  }

  @Test
  void migrateLibraries_positive_delegatesToServiceAndReturns204() {
    var dtos = List.of(new Library("Main Lib", "ML", CAMPUS_ID).id(LIBRARY_ID));

    var response = controller.migrateLibraries(dtos);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(libraryMigrationService).migrate(dtos);
    verifyNoMoreInteractions(institutionMigrationService, campusMigrationService, libraryMigrationService,
      locationMigrationService, servicePointMigrationService, servicePointUserMigrationService);
  }

  @Test
  void migrateLocations_positive_delegatesToServiceAndReturns204() {
    var dtos = List.of(
      new Location("Main", "ML", INSTITUTION_ID, CAMPUS_ID, LIBRARY_ID, SERVICE_POINT_ID).id(LOCATION_ID));

    var response = controller.migrateLocations(dtos);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(locationMigrationService).migrate(dtos);
    verifyNoMoreInteractions(institutionMigrationService, campusMigrationService, libraryMigrationService,
      locationMigrationService, servicePointMigrationService, servicePointUserMigrationService);
  }

  @Test
  void migrateServicePoints_positive_delegatesToServiceAndReturns204() {
    var dtos = List.of(new ServicePoint().id(SERVICE_POINT_ID));

    var response = controller.migrateServicePoints(dtos);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(servicePointMigrationService).migrate(dtos);
    verifyNoMoreInteractions(institutionMigrationService, campusMigrationService, libraryMigrationService,
      locationMigrationService, servicePointMigrationService, servicePointUserMigrationService);
  }

  @Test
  void migrateServicePointsUsers_positive_delegatesToServiceAndReturns204() {
    var dtos = List.of(new ServicePointsUser(USER_ID));

    var response = controller.migrateServicePointsUsers(dtos);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(servicePointUserMigrationService).migrate(dtos);
    verifyNoMoreInteractions(institutionMigrationService, campusMigrationService, libraryMigrationService,
      locationMigrationService, servicePointMigrationService, servicePointUserMigrationService);
  }
}
