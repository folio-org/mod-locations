package org.folio.locations.api;

import static org.folio.locations.support.ApiResourceUrls.campusResource;
import static org.folio.locations.support.ApiResourceUrls.campusesResource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.locations.domain.dto.Campus;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.support.ApiResourceUrls;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CampusIT extends BaseIT {

  private static UUID createInstitution() {
    var id = UUID.randomUUID();
    var name = "Inst-" + id.toString().substring(0, 8);
    var code = "I-" + id.toString().substring(0, 4);
    doPost(ApiResourceUrls.institutionsResource(), new Institution(name, code).id(id));
    return id;
  }

  private static Campus campus(String name, String code, UUID institutionId) {
    return new Campus(name, code, institutionId);
  }

  private static UUID createCampus(String name, String code, UUID institutionId) {
    var id = UUID.randomUUID();
    doPost(campusesResource(), campus(name, code, institutionId).id(id));
    return id;
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class CreateCampusTests {

    @Test
    void createCampus_positive_returnsCreatedWithMetadata() throws Exception {
      var instId = createInstitution();

      tryPost(campusesResource(), campus("City Campus", "CC", instId))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is("City Campus")))
        .andExpect(jsonPath("$.code", is("CC")))
        .andExpect(jsonPath("$.institutionId", is(instId.toString())))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createCampus_negative_missingName() throws Exception {
      var instId = createInstitution();
      var body = "{\"code\":\"CC\",\"institutionId\":\"" + instId + "\"}";

      tryPost(campusesResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")));
    }

    @Test
    void createCampus_negative_missingCode() throws Exception {
      var instId = createInstitution();
      var body = "{\"name\":\"City Campus\",\"institutionId\":\"" + instId + "\"}";

      tryPost(campusesResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("code")));
    }

    @Test
    void createCampus_negative_missingInstitutionId() throws Exception {
      var body = "{\"name\":\"City Campus\",\"code\":\"CC\"}";

      tryPost(campusesResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("institutionId")));
    }

    @Test
    void createCampus_negative_nonExistentInstitutionId() throws Exception {
      tryPost(campusesResource(), campus("City Campus", "CC", UUID.randomUUID()))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createCampus_negative_duplicateName() throws Exception {
      var instId = createInstitution();
      doPost(campusesResource(), campus("Unique Campus", "UC1", instId));

      tryPost(campusesResource(), campus("Unique Campus", "UC2", instId))
        .andExpect(status().isUnprocessableContent());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class GetCampusTests {

    @Test
    void getById_positive_returnsRecord() throws Exception {
      var instId = createInstitution();
      var id = createCampus("Get Me", "GM", instId);

      doGet(campusResource(id))
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Get Me")))
        .andExpect(jsonPath("$.institutionId", is(instId.toString())));
    }

    @Test
    void getById_negative_notFound() throws Exception {
      tryGet(campusResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }

    @Test
    void getAll_positive_returnsAll() throws Exception {
      var instId = createInstitution();
      createCampus("Campus A", "CA", instId);
      createCampus("Campus B", "CB", instId);

      doGet(campusesResource())
        .andExpect(jsonPath("$.totalRecords", is(2)))
        .andExpect(jsonPath("$.loccamps", hasSize(2)));
    }

    @Test
    void getAll_positive_queryByInstitutionId() throws Exception {
      var instId1 = createInstitution();
      var instId2 = createInstitution();
      createCampus("Alpha", "A", instId1);
      createCampus("Beta", "B", instId2);

      doGet(campusesResource() + "?query=institutionId==\"" + instId1 + "\"")
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.loccamps[0].name", is("Alpha")));
    }

    @Test
    void getAll_negative_invalidCql() throws Exception {
      tryGet(campusesResource() + "?query=invalid***cql")
        .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class UpdateCampusTests {

    @Test
    void updateCampus_positive_updatesRecord() throws Exception {
      var instId = createInstitution();
      var id = createCampus("Old Name", "OLD", instId);

      doPut(campusResource(id), campus("New Name", "NEW", instId).id(id));

      doGet(campusResource(id))
        .andExpect(jsonPath("$.name", is("New Name")))
        .andExpect(jsonPath("$.code", is("NEW")));
    }

    @Test
    void updateCampus_negative_notFound() throws Exception {
      var instId = createInstitution();
      tryPut(campusResource(UUID.randomUUID()), campus("New Name", "NEW", instId))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class DeleteCampusTests {

    @Test
    void deleteById_positive_deletesRecord() throws Exception {
      var instId = createInstitution();
      var id = createCampus("Delete Me", "DM", instId);

      doDelete(campusResource(id));

      tryGet(campusResource(id))
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_negative_notFound() throws Exception {
      tryDelete(campusResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class DeleteCampusesTests {

    @Test
    void deleteAll_positive_deletesAllRecords() throws Exception {
      var instId = createInstitution();
      createCampus("Campus 1", "C1", instId);
      createCampus("Campus 2", "C2", instId);

      doDelete(campusesResource());

      doGet(campusesResource())
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
