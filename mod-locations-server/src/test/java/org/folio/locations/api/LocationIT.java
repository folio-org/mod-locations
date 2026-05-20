package org.folio.locations.api;

import static org.folio.locations.service.validator.LocationValidator.ERR_DUPLICATE_SERVICE_POINTS;
import static org.folio.locations.service.validator.LocationValidator.ERR_NO_SERVICE_POINTS;
import static org.folio.locations.service.validator.LocationValidator.ERR_PRIMARY_NOT_IN_SERVICE_POINTS;
import static org.folio.locations.support.ApiResourceUrls.locationResource;
import static org.folio.locations.support.ApiResourceUrls.locationsResource;
import static org.folio.locations.support.TestFactoryHelper.createCampus;
import static org.folio.locations.support.TestFactoryHelper.createInstitution;
import static org.folio.locations.support.TestFactoryHelper.createLibrary;
import static org.folio.locations.support.TestFactoryHelper.createLocation;
import static org.folio.locations.support.TestFactoryHelper.createServicePoint;
import static org.folio.locations.support.TestFactoryHelper.location;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@IntegrationTest
class LocationIT extends BaseIT {

  // ── Factory helpers ───────────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(
    tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
              LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
              InstitutionEntity.INSTITUTION_TABLE, ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = TENANT_ID)
  class CreateLocationTests {

    @Test
    void createLocation_positive_returnsCreatedWithMetadata() throws Exception {
      var spId = createServicePoint("Circ Desk", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);

      tryPost(locationsResource(), TENANT_ID, location("Main Library", "ML", instId, campusId, libraryId, spId))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is("Main Library")))
        .andExpect(jsonPath("$.code", is("ML")))
        .andExpect(jsonPath("$.institutionId", is(instId.toString())))
        .andExpect(jsonPath("$.campusId", is(campusId.toString())))
        .andExpect(jsonPath("$.libraryId", is(libraryId.toString())))
        .andExpect(jsonPath("$.primaryServicePoint", is(spId.toString())))
        .andExpect(jsonPath("$.servicePointIds", hasSize(1)))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createLocation_negative_missingName() throws Exception {
      var spId = createServicePoint("Desk1", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var body = "{\"code\":\"ML\",\"institutionId\":\"" + instId + "\","
                 + "\"campusId\":\"" + campusId + "\","
                 + "\"libraryId\":\"" + libraryId + "\","
                 + "\"primaryServicePoint\":\"" + spId + "\","
                 + "\"servicePointIds\":[\"" + spId + "\"]}";

      tryPost(locationsResource(), TENANT_ID, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")));
    }

    @Test
    void createLocation_negative_missingCode() throws Exception {
      var spId = createServicePoint("Desk2", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var body = "{\"name\":\"Main\",\"institutionId\":\"" + instId + "\","
                 + "\"campusId\":\"" + campusId + "\","
                 + "\"libraryId\":\"" + libraryId + "\","
                 + "\"primaryServicePoint\":\"" + spId + "\","
                 + "\"servicePointIds\":[\"" + spId + "\"]}";

      tryPost(locationsResource(), TENANT_ID, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("code")));
    }

    @Test
    void createLocation_negative_missingInstitutionId() throws Exception {
      var spId = createServicePoint("Desk3", TENANT_ID);
      var campusId = createCampus(createInstitution(TENANT_ID), TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var body = "{\"name\":\"Main\",\"code\":\"ML\","
                 + "\"campusId\":\"" + campusId + "\","
                 + "\"libraryId\":\"" + libraryId + "\","
                 + "\"primaryServicePoint\":\"" + spId + "\","
                 + "\"servicePointIds\":[\"" + spId + "\"]}";

      tryPost(locationsResource(), TENANT_ID, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("institutionId")));
    }

    @Test
    void createLocation_negative_emptyServicePointIds() throws Exception {
      var spId = createServicePoint("Desk4", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var loc = location("Empty SP", "ESP", instId, campusId, libraryId, spId)
        .servicePointIds(List.of());

      tryPost(locationsResource(), TENANT_ID, loc)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_NO_SERVICE_POINTS)));
    }

    @Test
    void createLocation_negative_primaryNotInServicePoints() throws Exception {
      var spId1 = createServicePoint("Primary SP", TENANT_ID);
      var spId2 = createServicePoint("Other SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var loc = location("Bad Primary", "BP", instId, campusId, libraryId, spId1)
        .servicePointIds(List.of(spId2));

      tryPost(locationsResource(), TENANT_ID, loc)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_PRIMARY_NOT_IN_SERVICE_POINTS)));
    }

    @Test
    void createLocation_negative_duplicateServicePoints() throws Exception {
      var spId = createServicePoint("Dup SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var loc = location("Dup", "DUP", instId, campusId, libraryId, spId)
        .servicePointIds(List.of(spId, spId));

      tryPost(locationsResource(), TENANT_ID, loc)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_DUPLICATE_SERVICE_POINTS)));
    }

    @Test
    void createLocation_negative_duplicateName() throws Exception {
      var spId = createServicePoint("Dup Name SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      doPost(locationsResource(), TENANT_ID, location("Unique Loc", "UL1", instId, campusId, libraryId, spId));

      tryPost(locationsResource(), TENANT_ID, location("Unique Loc", "UL2", instId, campusId, libraryId, spId))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createLocation_negative_duplicateCode() throws Exception {
      var spId = createServicePoint("Dup Code SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      doPost(locationsResource(), TENANT_ID, location("Name One", "SAME-CODE", instId, campusId, libraryId, spId));

      tryPost(locationsResource(), TENANT_ID, location("Name Two", "SAME-CODE", instId, campusId, libraryId, spId))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createLocation_negative_nonExistentLibraryId() throws Exception {
      var spId = createServicePoint("FK SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);

      tryPost(locationsResource(),
        TENANT_ID, location("Bad FK", "BFKL", instId, campusId, UUID.randomUUID(), spId))
        .andExpect(status().isUnprocessableContent());
    }
  }

  @Nested
  @DatabaseCleanup(
    tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
              LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
              InstitutionEntity.INSTITUTION_TABLE, ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = TENANT_ID)
  class GetLocationTests {

    @Test
    void getById_positive_returnsRecord() throws Exception {
      var spId = createServicePoint("Get SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var id = createLocation("Get Me", "GM", instId, campusId, libraryId, spId, TENANT_ID);

      doGet(locationResource(id), TENANT_ID)
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Get Me")))
        .andExpect(jsonPath("$.servicePointIds", hasSize(1)));
    }

    @Test
    void getById_negative_notFound() throws Exception {
      tryGet(locationResource(UUID.randomUUID()), TENANT_ID)
        .andExpect(status().isNotFound());
    }

    @Test
    void getAll_positive_returnsAll() throws Exception {
      var spId = createServicePoint("GetAll SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      createLocation("Location A", "LOCA", instId, campusId, libraryId, spId, TENANT_ID);
      createLocation("Location B", "LOCB", instId, campusId, libraryId, spId, TENANT_ID);

      doGet(locationsResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(2)))
        .andExpect(jsonPath("$.locations", hasSize(2)));
    }

    @Test
    void getAll_positive_excludesShadowByDefault() throws Exception {
      var spId = createServicePoint("Shadow SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      createLocation("Regular Loc", "REG", instId, campusId, libraryId, spId, TENANT_ID);
      var shadowLoc = location("Shadow Loc", "SHAD", instId, campusId, libraryId, spId).isShadow(true);
      doPost(locationsResource(), TENANT_ID, shadowLoc);

      doGet(locationsResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locations[0].code", is("REG")));
    }

    @Test
    void getAll_positive_includeShadowTrue_returnsShadowLocations() throws Exception {
      var spId = createServicePoint("IncShad SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      createLocation("Loc1", "LC1", instId, campusId, libraryId, spId, TENANT_ID);
      var shadowLoc = location("Shadow Loc2", "SHAD2", instId, campusId, libraryId, spId).isShadow(true);
      doPost(locationsResource(), TENANT_ID, shadowLoc);

      doGet(locationsResource() + "?includeShadow=true", TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    void getAll_positive_cqlQueryByCode() throws Exception {
      var spId = createServicePoint("CQL SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      createLocation("Alpha", "ALPHA", instId, campusId, libraryId, spId, TENANT_ID);
      createLocation("Beta", "BETA", instId, campusId, libraryId, spId, TENANT_ID);

      doGet(locationsResource() + "?query=code==\"ALPHA\"", TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locations[0].name", is("Alpha")));
    }

    @Test
    void getAll_positive_cqlQueryByPrimaryServicePoint() throws Exception {
      var sp1 = createServicePoint("SP One", TENANT_ID);
      var sp2 = createServicePoint("SP Two", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      createLocation("Loc SP1", "LSP1", instId, campusId, libraryId, sp1, TENANT_ID);
      createLocation("Loc SP2", "LSP2", instId, campusId, libraryId, sp2, TENANT_ID);

      doGet(locationsResource() + "?query=primaryServicePoint==\"" + sp1 + "\"", TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locations[0].code", is("LSP1")));
    }

    @Test
    void getAll_negative_invalidCql() throws Exception {
      tryGet(locationsResource() + "?query=invalid***cql", TENANT_ID)
        .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DatabaseCleanup(
    tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
              LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
              InstitutionEntity.INSTITUTION_TABLE, ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = TENANT_ID)
  class UpdateLocationTests {

    @Test
    void updateLocation_positive_updatesRecord() throws Exception {
      var spId = createServicePoint("Update SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var id = createLocation("Old Name", "OLD", instId, campusId, libraryId, spId, TENANT_ID);

      doPut(locationResource(id), TENANT_ID, location("New Name", "NEW", instId, campusId, libraryId, spId).id(id));

      doGet(locationResource(id), TENANT_ID)
        .andExpect(jsonPath("$.name", is("New Name")))
        .andExpect(jsonPath("$.code", is("NEW")));
    }

    @Test
    void updateLocation_positive_replaceServicePoints() throws Exception {
      var sp1 = createServicePoint("Update SP Old", TENANT_ID);
      var sp2 = createServicePoint("Update SP New", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var id = createLocation("Replace SP", "REPLACE", instId, campusId, libraryId, sp1, TENANT_ID);

      var updated = location("Replace SP", "REPLACE", instId, campusId, libraryId, sp2).id(id);
      doPut(locationResource(id), TENANT_ID, updated);

      doGet(locationResource(id), TENANT_ID)
        .andExpect(jsonPath("$.servicePointIds", hasSize(1)))
        .andExpect(jsonPath("$.servicePointIds[0]", is(sp2.toString())))
        .andExpect(jsonPath("$.primaryServicePoint", is(sp2.toString())));
    }

    @Test
    void updateLocation_negative_notFound() throws Exception {
      var spId = createServicePoint("NF SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);

      tryPut(locationResource(UUID.randomUUID()),
        location("Not Found", "NF", instId, campusId, libraryId, spId), TENANT_ID)
        .andExpect(status().isNotFound());
    }

    @Test
    void updateLocation_negative_emptyServicePoints() throws Exception {
      var spId = createServicePoint("Empty Update SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var id = createLocation("Has SP", "HSP", instId, campusId, libraryId, spId, TENANT_ID);

      tryPut(locationResource(id),
        location("Has SP", "HSP", instId, campusId, libraryId, spId)
          .id(id).servicePointIds(List.of()), TENANT_ID)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_NO_SERVICE_POINTS)));
    }

    @Test
    void updateLocation_negative_multipleServicePointsMatchedCorrectly() throws Exception {
      var sp1 = createServicePoint("Multi SP1", TENANT_ID);
      var sp2 = createServicePoint("Multi SP2", TENANT_ID);
      var sp3 = createServicePoint("Multi SP3", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var id = createLocation("Multi", "MLT", instId, campusId, libraryId, sp1, TENANT_ID);

      doPut(locationResource(id),
        TENANT_ID, new Location("Multi", "MLT", instId, campusId, libraryId, sp1)
          .id(id)
          .servicePointIds(List.of(sp1, sp2, sp3)));

      doGet(locationResource(id), TENANT_ID)
        .andExpect(jsonPath("$.servicePointIds",
          containsInAnyOrder(sp1.toString(), sp2.toString(), sp3.toString())));
    }
  }

  @Nested
  @DatabaseCleanup(
    tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
              LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
              InstitutionEntity.INSTITUTION_TABLE, ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = TENANT_ID)
  class DeleteLocationTests {

    @Test
    void deleteById_positive_deletesRecord() throws Exception {
      var spId = createServicePoint("Del SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      var id = createLocation("Delete Me", "DM", instId, campusId, libraryId, spId, TENANT_ID);

      doDelete(locationResource(id), TENANT_ID);

      tryGet(locationResource(id), TENANT_ID)
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_negative_notFound() throws Exception {
      tryDelete(locationResource(UUID.randomUUID()), TENANT_ID)
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(
    tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
              LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
              InstitutionEntity.INSTITUTION_TABLE, ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = TENANT_ID)
  class DeleteLocationsTests {

    @Test
    void deleteAll_positive_deletesAllRecords() throws Exception {
      var spId = createServicePoint("DelAll SP", TENANT_ID);
      var instId = createInstitution(TENANT_ID);
      var campusId = createCampus(instId, TENANT_ID);
      var libraryId = createLibrary(campusId, TENANT_ID);
      createLocation("Loc 1", "DEL1", instId, campusId, libraryId, spId, TENANT_ID);
      createLocation("Loc 2", "DEL2", instId, campusId, libraryId, spId, TENANT_ID);

      doDelete(locationsResource(), TENANT_ID);

      doGet(locationsResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
