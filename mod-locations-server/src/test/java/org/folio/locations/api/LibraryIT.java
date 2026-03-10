package org.folio.locations.api;

import static org.folio.locations.support.ApiResourceUrls.librariesResource;
import static org.folio.locations.support.ApiResourceUrls.libraryResource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.dto.Library;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.support.ApiResourceUrls;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@IntegrationTest
class LibraryIT extends BaseIT {

  private static UUID createInstitution() {
    var id = UUID.randomUUID();
    doPost(ApiResourceUrls.institutionsResource(),
      new Institution("Inst-" + id.toString().substring(0, 8), "I-" + id.toString().substring(0, 4)).id(id));
    return id;
  }

  private static UUID createCampus(UUID institutionId) {
    var id = UUID.randomUUID();
    doPost(ApiResourceUrls.campusesResource(),
      new Campus("Camp-" + id.toString().substring(0, 8), "C-" + id.toString().substring(0, 4), institutionId).id(id));
    return id;
  }

  private static Library library(String name, String code, UUID campusId) {
    return new Library(name, code, campusId);
  }

  private static UUID createLibrary(String name, String code, UUID campusId) {
    var id = UUID.randomUUID();
    doPost(librariesResource(), library(name, code, campusId).id(id));
    return id;
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class CreateLibraryTests {

    @Test
    void createLibrary_positive_returnsCreatedWithMetadata() throws Exception {
      var campusId = createCampus(createInstitution());

      tryPost(librariesResource(), library("Main Library", "ML", campusId))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is("Main Library")))
        .andExpect(jsonPath("$.code", is("ML")))
        .andExpect(jsonPath("$.campusId", is(campusId.toString())))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createLibrary_negative_missingName() throws Exception {
      var campusId = createCampus(createInstitution());
      var body = "{\"code\":\"ML\",\"campusId\":\"" + campusId + "\"}";

      tryPost(librariesResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")));
    }

    @Test
    void createLibrary_negative_missingCode() throws Exception {
      var campusId = createCampus(createInstitution());
      var body = "{\"name\":\"Main Library\",\"campusId\":\"" + campusId + "\"}";

      tryPost(librariesResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("code")));
    }

    @Test
    void createLibrary_negative_missingCampusId() throws Exception {
      var body = "{\"name\":\"Main Library\",\"code\":\"ML\"}";

      tryPost(librariesResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("campusId")));
    }

    @Test
    void createLibrary_negative_nonExistentCampusId() throws Exception {
      tryPost(librariesResource(), library("Main Library", "ML", UUID.randomUUID()))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createLibrary_negative_duplicateName() throws Exception {
      var campusId = createCampus(createInstitution());
      doPost(librariesResource(), library("Unique Library", "UL1", campusId));

      tryPost(librariesResource(), library("Unique Library", "UL2", campusId))
        .andExpect(status().isUnprocessableContent());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class GetLibraryTests {

    @Test
    void getById_positive_returnsRecord() throws Exception {
      var campusId = createCampus(createInstitution());
      var id = createLibrary("Get Me", "GM", campusId);

      doGet(libraryResource(id))
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Get Me")))
        .andExpect(jsonPath("$.campusId", is(campusId.toString())));
    }

    @Test
    void getById_negative_notFound() throws Exception {
      tryGet(libraryResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }

    @Test
    void getAll_positive_returnsAll() throws Exception {
      var campusId = createCampus(createInstitution());
      createLibrary("Library A", "LA", campusId);
      createLibrary("Library B", "LB", campusId);

      doGet(librariesResource())
        .andExpect(jsonPath("$.totalRecords", is(2)))
        .andExpect(jsonPath("$.loclibs", hasSize(2)));
    }

    @Test
    void getAll_positive_queryByCampusId() throws Exception {
      var campusId1 = createCampus(createInstitution());
      var campusId2 = createCampus(createInstitution());
      createLibrary("Alpha Lib", "AL", campusId1);
      createLibrary("Beta Lib", "BL", campusId2);

      doGet(librariesResource() + "?query=campusId==\"" + campusId1 + "\"")
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.loclibs[0].name", is("Alpha Lib")));
    }

    @Test
    void getAll_negative_invalidCql() throws Exception {
      tryGet(librariesResource() + "?query=invalid***cql")
        .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class UpdateLibraryTests {

    @Test
    void updateLibrary_positive_updatesRecord() throws Exception {
      var campusId = createCampus(createInstitution());
      var id = createLibrary("Old Name", "OLD", campusId);

      doPut(libraryResource(id), library("New Name", "NEW", campusId).id(id));

      doGet(libraryResource(id))
        .andExpect(jsonPath("$.name", is("New Name")))
        .andExpect(jsonPath("$.code", is("NEW")));
    }

    @Test
    void updateLibrary_negative_notFound() throws Exception {
      var campusId = createCampus(createInstitution());
      tryPut(libraryResource(UUID.randomUUID()), library("New Name", "NEW", campusId))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class DeleteLibraryTests {

    @Test
    void deleteById_positive_deletesRecord() throws Exception {
      var campusId = createCampus(createInstitution());
      var id = createLibrary("Delete Me", "DM", campusId);

      doDelete(libraryResource(id));

      tryGet(libraryResource(id))
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_negative_notFound() throws Exception {
      tryDelete(libraryResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class DeleteLibrariesTests {

    @Test
    void deleteAll_positive_deletesAllRecords() throws Exception {
      var campusId = createCampus(createInstitution());
      createLibrary("Library 1", "L1", campusId);
      createLibrary("Library 2", "L2", campusId);

      doDelete(librariesResource());

      doGet(librariesResource())
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
