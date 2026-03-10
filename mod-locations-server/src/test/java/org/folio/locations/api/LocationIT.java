package org.folio.locations.api;

import static org.folio.locations.service.validator.LocationValidator.ERR_DUPLICATE_SERVICE_POINTS;
import static org.folio.locations.service.validator.LocationValidator.ERR_NO_SERVICE_POINTS;
import static org.folio.locations.service.validator.LocationValidator.ERR_PRIMARY_NOT_IN_SERVICE_POINTS;
import static org.folio.locations.support.ApiResourceUrls.locationResource;
import static org.folio.locations.support.ApiResourceUrls.locationsResource;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.dto.Location;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.domain.entity.LocationEntity;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.support.ApiResourceUrls;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@IntegrationTest
class LocationIT extends BaseIT {

  // ── Factory helpers ───────────────────────────────────────────────────────────

  private static UUID createServicePoint(String name) {
    var id = UUID.randomUUID();
    doPost(ApiResourceUrls.servicePointsResource(),
      new ServicePoint(name, name.toLowerCase().replace(" ", "-"), "Display: " + name).id(id));
    return id;
  }

  private static UUID createInstitution() {
    var id = UUID.randomUUID();
    doPost(ApiResourceUrls.institutionsResource(),
      new Institution("Inst-" + id.toString().substring(0, 8), "I-" + id.toString().substring(0, 4)).id(id));
    return id;
  }

  private static UUID createCampus(UUID institutionId) {
    var id = UUID.randomUUID();
    doPost(ApiResourceUrls.campusesResource(),
      new Campus("Camp-" + id.toString().substring(0, 8), "C-" + id.toString().substring(0, 4),
        institutionId).id(id));
    return id;
  }

  private static UUID createLibrary(UUID campusId) {
    var id = UUID.randomUUID();
    doPost(ApiResourceUrls.librariesResource(),
      new Library("Lib-" + id.toString().substring(0, 8), "L-" + id.toString().substring(0, 4), campusId).id(id));
    return id;
  }

  private static Location location(String name, String code, UUID institutionId, UUID campusId, UUID libraryId,
                                   UUID primaryServicePointId) {
    return new Location(name, code, institutionId, campusId, libraryId, primaryServicePointId)
      .servicePointIds(List.of(primaryServicePointId));
  }

  private static UUID createLocation(String name, String code, UUID institutionId, UUID campusId, UUID libraryId,
                                     UUID spId) {
    var id = UUID.randomUUID();
    doPost(locationsResource(),
      location(name, code, institutionId, campusId, libraryId, spId).id(id));
    return id;
  }

  @Nested
  @DatabaseCleanup(
    tables = {LocationEntity.LOCATION_SERVICE_POINT_TABLE, LocationEntity.LOCATION_TABLE,
              LibraryEntity.LIBRARY_TABLE, CampusEntity.CAMPUS_TABLE,
              InstitutionEntity.INSTITUTION_TABLE, ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = TENANT_ID)
  class CreateLocationTests {

    @Test
    void createLocation_positive_returnsCreatedWithMetadata() throws Exception {
      var spId = createServicePoint("Circ Desk");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);

      tryPost(locationsResource(), location("Main Library", "ML", instId, campusId, libraryId, spId))
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
      var spId = createServicePoint("Desk1");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var body = "{\"code\":\"ML\",\"institutionId\":\"" + instId + "\","
        + "\"campusId\":\"" + campusId + "\","
        + "\"libraryId\":\"" + libraryId + "\","
        + "\"primaryServicePoint\":\"" + spId + "\","
        + "\"servicePointIds\":[\"" + spId + "\"]}";

      tryPost(locationsResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")));
    }

    @Test
    void createLocation_negative_missingCode() throws Exception {
      var spId = createServicePoint("Desk2");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var body = "{\"name\":\"Main\",\"institutionId\":\"" + instId + "\","
        + "\"campusId\":\"" + campusId + "\","
        + "\"libraryId\":\"" + libraryId + "\","
        + "\"primaryServicePoint\":\"" + spId + "\","
        + "\"servicePointIds\":[\"" + spId + "\"]}";

      tryPost(locationsResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("code")));
    }

    @Test
    void createLocation_negative_missingInstitutionId() throws Exception {
      var spId = createServicePoint("Desk3");
      var campusId = createCampus(createInstitution());
      var libraryId = createLibrary(campusId);
      var body = "{\"name\":\"Main\",\"code\":\"ML\","
        + "\"campusId\":\"" + campusId + "\","
        + "\"libraryId\":\"" + libraryId + "\","
        + "\"primaryServicePoint\":\"" + spId + "\","
        + "\"servicePointIds\":[\"" + spId + "\"]}";

      tryPost(locationsResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("institutionId")));
    }

    @Test
    void createLocation_negative_emptyServicePointIds() throws Exception {
      var spId = createServicePoint("Desk4");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var loc = location("Empty SP", "ESP", instId, campusId, libraryId, spId)
        .servicePointIds(List.of());

      tryPost(locationsResource(), loc)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_NO_SERVICE_POINTS)));
    }

    @Test
    void createLocation_negative_primaryNotInServicePoints() throws Exception {
      var spId1 = createServicePoint("Primary SP");
      var spId2 = createServicePoint("Other SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var loc = location("Bad Primary", "BP", instId, campusId, libraryId, spId1)
        .servicePointIds(List.of(spId2));

      tryPost(locationsResource(), loc)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_PRIMARY_NOT_IN_SERVICE_POINTS)));
    }

    @Test
    void createLocation_negative_duplicateServicePoints() throws Exception {
      var spId = createServicePoint("Dup SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var loc = location("Dup", "DUP", instId, campusId, libraryId, spId)
        .servicePointIds(List.of(spId, spId));

      tryPost(locationsResource(), loc)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_DUPLICATE_SERVICE_POINTS)));
    }

    @Test
    void createLocation_negative_duplicateName() throws Exception {
      var spId = createServicePoint("Dup Name SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      doPost(locationsResource(), location("Unique Loc", "UL1", instId, campusId, libraryId, spId));

      tryPost(locationsResource(), location("Unique Loc", "UL2", instId, campusId, libraryId, spId))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createLocation_negative_duplicateCode() throws Exception {
      var spId = createServicePoint("Dup Code SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      doPost(locationsResource(), location("Name One", "SAME-CODE", instId, campusId, libraryId, spId));

      tryPost(locationsResource(), location("Name Two", "SAME-CODE", instId, campusId, libraryId, spId))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createLocation_negative_nonExistentLibraryId() throws Exception {
      var spId = createServicePoint("FK SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);

      tryPost(locationsResource(),
        location("Bad FK", "BFKL", instId, campusId, UUID.randomUUID(), spId))
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
      var spId = createServicePoint("Get SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var id = createLocation("Get Me", "GM", instId, campusId, libraryId, spId);

      doGet(locationResource(id))
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Get Me")))
        .andExpect(jsonPath("$.servicePointIds", hasSize(1)));
    }

    @Test
    void getById_negative_notFound() throws Exception {
      tryGet(locationResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }

    @Test
    void getAll_positive_returnsAll() throws Exception {
      var spId = createServicePoint("GetAll SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      createLocation("Location A", "LOCA", instId, campusId, libraryId, spId);
      createLocation("Location B", "LOCB", instId, campusId, libraryId, spId);

      doGet(locationsResource())
        .andExpect(jsonPath("$.totalRecords", is(2)))
        .andExpect(jsonPath("$.locations", hasSize(2)));
    }

    @Test
    void getAll_positive_excludesShadowByDefault() throws Exception {
      var spId = createServicePoint("Shadow SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      createLocation("Regular Loc", "REG", instId, campusId, libraryId, spId);
      var shadowLoc = location("Shadow Loc", "SHAD", instId, campusId, libraryId, spId).isShadow(true);
      doPost(locationsResource(), shadowLoc);

      doGet(locationsResource())
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locations[0].code", is("REG")));
    }

    @Test
    void getAll_positive_includeShadowTrue_returnsShadowLocations() throws Exception {
      var spId = createServicePoint("IncShad SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      createLocation("Loc1", "LC1", instId, campusId, libraryId, spId);
      var shadowLoc = location("Shadow Loc2", "SHAD2", instId, campusId, libraryId, spId).isShadow(true);
      doPost(locationsResource(), shadowLoc);

      doGet(locationsResource() + "?includeShadow=true")
        .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    void getAll_positive_cqlQueryByCode() throws Exception {
      var spId = createServicePoint("CQL SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      createLocation("Alpha", "ALPHA", instId, campusId, libraryId, spId);
      createLocation("Beta", "BETA", instId, campusId, libraryId, spId);

      doGet(locationsResource() + "?query=code==\"ALPHA\"")
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locations[0].name", is("Alpha")));
    }

    @Test
    void getAll_positive_cqlQueryByPrimaryServicePoint() throws Exception {
      var sp1 = createServicePoint("SP One");
      var sp2 = createServicePoint("SP Two");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      createLocation("Loc SP1", "LSP1", instId, campusId, libraryId, sp1);
      createLocation("Loc SP2", "LSP2", instId, campusId, libraryId, sp2);

      doGet(locationsResource() + "?query=primaryServicePoint==\"" + sp1 + "\"")
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locations[0].code", is("LSP1")));
    }

    @Test
    void getAll_negative_invalidCql() throws Exception {
      tryGet(locationsResource() + "?query=invalid***cql")
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
      var spId = createServicePoint("Update SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var id = createLocation("Old Name", "OLD", instId, campusId, libraryId, spId);

      doPut(locationResource(id), location("New Name", "NEW", instId, campusId, libraryId, spId).id(id));

      doGet(locationResource(id))
        .andExpect(jsonPath("$.name", is("New Name")))
        .andExpect(jsonPath("$.code", is("NEW")));
    }

    @Test
    void updateLocation_positive_replaceServicePoints() throws Exception {
      var sp1 = createServicePoint("Update SP Old");
      var sp2 = createServicePoint("Update SP New");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var id = createLocation("Replace SP", "REPLACE", instId, campusId, libraryId, sp1);

      var updated = location("Replace SP", "REPLACE", instId, campusId, libraryId, sp2).id(id);
      doPut(locationResource(id), updated);

      doGet(locationResource(id))
        .andExpect(jsonPath("$.servicePointIds", hasSize(1)))
        .andExpect(jsonPath("$.servicePointIds[0]", is(sp2.toString())))
        .andExpect(jsonPath("$.primaryServicePoint", is(sp2.toString())));
    }

    @Test
    void updateLocation_negative_notFound() throws Exception {
      var spId = createServicePoint("NF SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);

      tryPut(locationResource(UUID.randomUUID()),
        location("Not Found", "NF", instId, campusId, libraryId, spId))
        .andExpect(status().isNotFound());
    }

    @Test
    void updateLocation_negative_emptyServicePoints() throws Exception {
      var spId = createServicePoint("Empty Update SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var id = createLocation("Has SP", "HSP", instId, campusId, libraryId, spId);

      tryPut(locationResource(id),
        location("Has SP", "HSP", instId, campusId, libraryId, spId)
          .id(id).servicePointIds(List.of()))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].message", is(ERR_NO_SERVICE_POINTS)));
    }

    @Test
    void updateLocation_negative_multipleServicePointsMatchedCorrectly() throws Exception {
      var sp1 = createServicePoint("Multi SP1");
      var sp2 = createServicePoint("Multi SP2");
      var sp3 = createServicePoint("Multi SP3");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var id = createLocation("Multi", "MLT", instId, campusId, libraryId, sp1);

      doPut(locationResource(id),
        new Location("Multi", "MLT", instId, campusId, libraryId, sp1)
          .id(id)
          .servicePointIds(List.of(sp1, sp2, sp3)));

      doGet(locationResource(id))
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
      var spId = createServicePoint("Del SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      var id = createLocation("Delete Me", "DM", instId, campusId, libraryId, spId);

      doDelete(locationResource(id));

      tryGet(locationResource(id))
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_negative_notFound() throws Exception {
      tryDelete(locationResource(UUID.randomUUID()))
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
      var spId = createServicePoint("DelAll SP");
      var instId = createInstitution();
      var campusId = createCampus(instId);
      var libraryId = createLibrary(campusId);
      createLocation("Loc 1", "DEL1", instId, campusId, libraryId, spId);
      createLocation("Loc 2", "DEL2", instId, campusId, libraryId, spId);

      doDelete(locationsResource());

      doGet(locationsResource())
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
