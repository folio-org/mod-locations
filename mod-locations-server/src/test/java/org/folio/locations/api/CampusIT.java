package org.folio.locations.api;

import static org.folio.locations.support.ApiResourceUrls.campusResource;
import static org.folio.locations.support.ApiResourceUrls.campusesResource;
import static org.folio.locations.support.TestFactoryHelper.campus;
import static org.folio.locations.support.TestFactoryHelper.createCampus;
import static org.folio.locations.support.TestFactoryHelper.createInstitution;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@IntegrationTest
class CampusIT extends BaseIT {

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class CreateCampusTests {

    @Test
    void createCampus_positive_returnsCreatedWithMetadata() throws Exception {
      var instId = createInstitution(TENANT_ID);

      tryPost(campusesResource(), TENANT_ID, campus("City Campus", "CC", instId))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is("City Campus")))
        .andExpect(jsonPath("$.code", is("CC")))
        .andExpect(jsonPath("$.institutionId", is(instId.toString())))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createCampus_negative_missingName() throws Exception {
      var instId = createInstitution(TENANT_ID);
      var body = "{\"code\":\"CC\",\"institutionId\":\"" + instId + "\"}";

      tryPost(campusesResource(), TENANT_ID, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")));
    }

    @Test
    void createCampus_negative_missingCode() throws Exception {
      var instId = createInstitution(TENANT_ID);
      var body = "{\"name\":\"City Campus\",\"institutionId\":\"" + instId + "\"}";

      tryPost(campusesResource(), TENANT_ID, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("code")));
    }

    @Test
    void createCampus_negative_missingInstitutionId() throws Exception {
      var body = "{\"name\":\"City Campus\",\"code\":\"CC\"}";

      tryPost(campusesResource(), TENANT_ID, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("institutionId")));
    }

    @Test
    void createCampus_negative_nonExistentInstitutionId() throws Exception {
      tryPost(campusesResource(), TENANT_ID, campus("City Campus", "CC", UUID.randomUUID()))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createCampus_negative_duplicateName() throws Exception {
      var instId = createInstitution(TENANT_ID);
      doPost(campusesResource(), TENANT_ID, campus("Unique Campus", "UC1", instId));

      tryPost(campusesResource(), TENANT_ID, campus("Unique Campus", "UC2", instId))
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
      var instId = createInstitution(TENANT_ID);
      var id = createCampus("Get Me", "GM", instId, TENANT_ID);

      doGet(campusResource(id), TENANT_ID)
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Get Me")))
        .andExpect(jsonPath("$.institutionId", is(instId.toString())));
    }

    @Test
    void getById_negative_notFound() throws Exception {
      tryGet(campusResource(UUID.randomUUID()), TENANT_ID)
        .andExpect(status().isNotFound());
    }

    @Test
    void getAll_positive_returnsAll() throws Exception {
      var instId = createInstitution(TENANT_ID);
      createCampus("Campus A", "CA", instId, TENANT_ID);
      createCampus("Campus B", "CB", instId, TENANT_ID);

      doGet(campusesResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(2)))
        .andExpect(jsonPath("$.loccamps", hasSize(2)));
    }

    @Test
    void getAll_positive_queryByInstitutionId() throws Exception {
      var instId1 = createInstitution(TENANT_ID);
      var instId2 = createInstitution(TENANT_ID);
      createCampus("Alpha", "A", instId1, TENANT_ID);
      createCampus("Beta", "B", instId2, TENANT_ID);

      doGet(campusesResource() + "?query=institutionId==\"" + instId1 + "\"", TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.loccamps[0].name", is("Alpha")));
    }

    @Test
    void getAll_negative_invalidCql() throws Exception {
      tryGet(campusesResource() + "?query=invalid***cql", TENANT_ID)
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
      var instId = createInstitution(TENANT_ID);
      var id = createCampus("Old Name", "OLD", instId, TENANT_ID);

      doPut(campusResource(id), TENANT_ID, campus("New Name", "NEW", instId).id(id));

      doGet(campusResource(id), TENANT_ID)
        .andExpect(jsonPath("$.name", is("New Name")))
        .andExpect(jsonPath("$.code", is("NEW")));
    }

    @Test
    void updateCampus_negative_notFound() throws Exception {
      var instId = createInstitution(TENANT_ID);
      tryPut(campusResource(UUID.randomUUID()), campus("New Name", "NEW", instId), TENANT_ID)
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
      var instId = createInstitution(TENANT_ID);
      var id = createCampus("Delete Me", "DM", instId, TENANT_ID);

      doDelete(campusResource(id), TENANT_ID);

      tryGet(campusResource(id), TENANT_ID)
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_negative_notFound() throws Exception {
      tryDelete(campusResource(UUID.randomUUID()), TENANT_ID)
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
      var instId = createInstitution(TENANT_ID);
      createCampus("Campus 1", "C1", instId, TENANT_ID);
      createCampus("Campus 2", "C2", instId, TENANT_ID);

      doDelete(campusesResource(), TENANT_ID);

      doGet(campusesResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
