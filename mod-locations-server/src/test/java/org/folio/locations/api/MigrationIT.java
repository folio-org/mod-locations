package org.folio.locations.api;

import static org.folio.locations.support.ApiResourceUrls.campusResource;
import static org.folio.locations.support.ApiResourceUrls.institutionResource;
import static org.folio.locations.support.ApiResourceUrls.institutionsResource;
import static org.folio.locations.support.ApiResourceUrls.libraryResource;
import static org.folio.locations.support.ApiResourceUrls.locationResource;
import static org.folio.locations.support.ApiResourceUrls.migrateCampusesResource;
import static org.folio.locations.support.ApiResourceUrls.migrateInstitutionsResource;
import static org.folio.locations.support.ApiResourceUrls.migrateLibrariesResource;
import static org.folio.locations.support.ApiResourceUrls.migrateLocationsResource;
import static org.folio.locations.support.ApiResourceUrls.migrateServicePointsResource;
import static org.folio.locations.support.ApiResourceUrls.migrateServicePointsUsersResource;
import static org.folio.locations.support.ApiResourceUrls.servicePointResource;
import static org.folio.locations.support.ApiResourceUrls.servicePointsUsersResource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.Metadata;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointStaffSlip;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipEntity;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@IntegrationTest
class MigrationIT extends BaseIT {

  private static final String LEGACY_DATE_STR = "2020-01-15T10:00:00Z";
  private static final OffsetDateTime LEGACY_DATE = OffsetDateTime.parse(LEGACY_DATE_STR);

  private static Metadata legacyMetadata() {
    return new Metadata()
      .createdDate(LEGACY_DATE)
      .createdByUserId(UUID.fromString(USER_ID))
      .updatedDate(LEGACY_DATE)
      .updatedByUserId(UUID.fromString(USER_ID));
  }

  // ── /migrate/institutions ─────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class MigrateInstitutionsTests {

    @Test
    void migrateInstitutions_positive_returnsNoContent() throws Exception {
      var dto = new Institution("Migrated Inst", "MI").id(UUID.randomUUID());

      tryPost(migrateInstitutionsResource(), TENANT_ID, List.of(dto))
        .andExpect(status().isNoContent());
    }

    @Test
    void migrateInstitutions_positive_recordIsPersisted() throws Exception {
      var id = UUID.randomUUID();
      var dto = new Institution("Persisted Inst", "PI").id(id);

      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(dto));

      doGet(institutionResource(id), TENANT_ID)
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Persisted Inst")))
        .andExpect(jsonPath("$.code", is("PI")));
    }

    @Test
    void migrateInstitutions_positive_metadataPreserved() throws Exception {
      var id = UUID.randomUUID();
      var dto = new Institution("Meta Inst", "META").id(id).metadata(legacyMetadata());

      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(dto));

      doGet(institutionResource(id), TENANT_ID)
        .andExpect(jsonPath("$.metadata.createdDate", is(LEGACY_DATE_STR)));
    }

    @Test
    void migrateInstitutions_positive_multipleRecordsPersisted() throws Exception {
      var id1 = UUID.randomUUID();
      var id2 = UUID.randomUUID();

      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(
        new Institution("Inst A", "IA").id(id1),
        new Institution("Inst B", "IB").id(id2)
      ));

      doGet(institutionsResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    void migrateInstitutions_positive_emptyListReturnsNoContent() throws Exception {
      tryPost(migrateInstitutionsResource(), TENANT_ID, List.of())
        .andExpect(status().isNoContent());
    }

    @Test
    void migrateInstitutions_positive_idempotent() throws Exception {
      var id = UUID.randomUUID();
      var dto = new Institution("Idempotent Inst", "II").id(id);

      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(dto));
      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(dto));

      doGet(institutionsResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)));
    }
  }

  // ── /migrate/campuses ─────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class MigrateCampusesTests {

    @Test
    void migrateCampuses_positive_recordIsPersisted() throws Exception {
      var instId = UUID.randomUUID();
      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(new Institution("Inst", "I").id(instId)));

      var campusId = UUID.randomUUID();
      doPost(migrateCampusesResource(), TENANT_ID, List.of(new Campus("Migrated Campus", "MC", instId).id(campusId)));

      doGet(campusResource(campusId), TENANT_ID)
        .andExpect(jsonPath("$.id", is(campusId.toString())))
        .andExpect(jsonPath("$.name", is("Migrated Campus")));
    }

    @Test
    void migrateCampuses_positive_metadataPreserved() throws Exception {
      var instId = UUID.randomUUID();
      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(new Institution("Inst2", "I2").id(instId)));

      var campusId = UUID.randomUUID();
      doPost(migrateCampusesResource(),
        TENANT_ID, List.of(new Campus("Meta Campus", "MCC", instId).id(campusId).metadata(legacyMetadata())));

      doGet(campusResource(campusId), TENANT_ID)
        .andExpect(jsonPath("$.metadata.createdDate", is(LEGACY_DATE_STR)));
    }
  }

  // ── /migrate/libraries ────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class MigrateLibrariesTests {

    @Test
    void migrateLibraries_positive_recordIsPersisted() throws Exception {
      var instId = UUID.randomUUID();
      var campusId = UUID.randomUUID();
      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(new Institution("Inst", "I3").id(instId)));
      doPost(migrateCampusesResource(), TENANT_ID, List.of(new Campus("Camp", "C3", instId).id(campusId)));

      var libId = UUID.randomUUID();
      doPost(migrateLibrariesResource(), TENANT_ID, List.of(new Library("Migrated Lib", "ML", campusId).id(libId)));

      doGet(libraryResource(libId), TENANT_ID)
        .andExpect(jsonPath("$.id", is(libId.toString())))
        .andExpect(jsonPath("$.name", is("Migrated Lib")));
    }
  }

  // ── /migrate/service-points ───────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class MigrateServicePointsTests {

    @Test
    void migrateServicePoints_positive_recordIsPersisted() throws Exception {
      var id = UUID.randomUUID();
      var dto = new ServicePoint("Migrated SP", "msp", "Migrated Service Point").id(id);

      doPost(migrateServicePointsResource(), TENANT_ID, List.of(dto));

      doGet(servicePointResource(id), TENANT_ID)
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Migrated SP")));
    }

    @Test
    void migrateServicePoints_positive_staffSlipsPersisted() throws Exception {
      var id = UUID.randomUUID();
      var slipId = UUID.randomUUID();
      var dto = new ServicePoint("SP With Slips", "spws", "Display")
        .id(id)
        .staffSlips(List.of(new ServicePointStaffSlip(slipId, true)));

      doPost(migrateServicePointsResource(), TENANT_ID, List.of(dto));

      doGet(servicePointResource(id), TENANT_ID)
        .andExpect(jsonPath("$.staffSlips", hasSize(1)))
        .andExpect(jsonPath("$.staffSlips[0].id", is(slipId.toString())))
        .andExpect(jsonPath("$.staffSlips[0].printByDefault", is(true)));
    }

    @Test
    void migrateServicePoints_positive_metadataPreserved() throws Exception {
      var id = UUID.randomUUID();
      var dto = new ServicePoint("Meta SP", "metasp", "Display")
        .id(id)
        .metadata(legacyMetadata());

      doPost(migrateServicePointsResource(), TENANT_ID, List.of(dto));

      doGet(servicePointResource(id), TENANT_ID)
        .andExpect(jsonPath("$.metadata.createdDate", is(LEGACY_DATE_STR)));
    }
  }

  // ── /migrate/locations ────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
                             ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE,
                             LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class MigrateLocationsTests {

    @Test
    void migrateLocations_positive_recordIsPersisted() throws Exception {
      final var spId = UUID.randomUUID();
      final var instId = UUID.randomUUID();
      final var campusId = UUID.randomUUID();
      final var libId = UUID.randomUUID();
      doPost(migrateServicePointsResource(),
        TENANT_ID, List.of(new ServicePoint("SP", "sp", "Display").id(spId)));
      doPost(migrateInstitutionsResource(), TENANT_ID, List.of(new Institution("Inst", "I4").id(instId)));
      doPost(migrateCampusesResource(), TENANT_ID, List.of(new Campus("Camp", "C4", instId).id(campusId)));
      doPost(migrateLibrariesResource(), TENANT_ID, List.of(new Library("Lib", "L4", campusId).id(libId)));

      var locId = UUID.randomUUID();
      doPost(migrateLocationsResource(), TENANT_ID, List.of(
        new Location("Migrated Loc", "ML", instId, campusId, libId, spId)
          .id(locId)
          .servicePointIds(List.of(spId))
      ));

      doGet(locationResource(locId), TENANT_ID)
        .andExpect(jsonPath("$.id", is(locId.toString())))
        .andExpect(jsonPath("$.name", is("Migrated Loc")));
    }
  }

  // ── /migrate/service-points-users ─────────────────────────────────────────

  @Nested
  @DatabaseCleanup(tables = {ServicePointUserEntity.SERVICE_POINT_USER_SERVICE_POINT_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_TABLE,
                             ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class MigrateServicePointsUsersTests {

    @Test
    void migrateServicePointsUsers_positive_recordIsPersisted() throws Exception {
      var spId = UUID.randomUUID();
      doPost(migrateServicePointsResource(),
        TENANT_ID, List.of(new ServicePoint("SP for User", "spfu", "Display").id(spId)));

      var userId = UUID.randomUUID();
      var spuId = UUID.randomUUID();
      doPost(migrateServicePointsUsersResource(), TENANT_ID, List.of(
        new ServicePointsUser(userId).id(spuId).servicePointsIds(List.of(spId))
      ));

      doGet(servicePointsUsersResource() + "?query=userId==\"" + userId + "\"", TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.servicePointsUsers[0].id", is(spuId.toString())));
    }

    @Test
    void migrateServicePointsUsers_positive_metadataPreserved() throws Exception {
      var spId = UUID.randomUUID();
      doPost(migrateServicePointsResource(),
        TENANT_ID, List.of(new ServicePoint("SP for Meta User", "spmeta", "Display").id(spId)));

      var userId = UUID.randomUUID();
      var spuId = UUID.randomUUID();
      doPost(migrateServicePointsUsersResource(), TENANT_ID, List.of(
        new ServicePointsUser(userId).id(spuId).metadata(legacyMetadata())
      ));

      doGet(servicePointsUsersResource() + "?query=userId==\"" + userId + "\"", TENANT_ID)
        .andExpect(jsonPath("$.servicePointsUsers[0].metadata.createdDate", is(LEGACY_DATE_STR)));
    }
  }
}
