package org.folio.locations.api;

import static org.folio.locations.support.ApiResourceUrls.institutionResource;
import static org.folio.locations.support.ApiResourceUrls.institutionsResource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.locations.domain.dto.Institution;
import org.folio.locations.domain.entity.CampusEntity;
import org.folio.locations.domain.entity.InstitutionEntity;
import org.folio.locations.domain.entity.LibraryEntity;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InstitutionIT extends BaseIT {

  private static Institution newInstitution(String name, String code) {
    return new Institution(name, code);
  }

  private static UUID createInstitution(String name, String code) {
    var id = UUID.randomUUID();
    doPost(institutionsResource(), newInstitution(name, code).id(id));
    return id;
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class CreateInstitutionTests {

    @Test
    void createInstitution_positive_returnsCreatedWithMetadata() throws Exception {
      tryPost(institutionsResource(), newInstitution("Main Institution", "MAIN"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is("Main Institution")))
        .andExpect(jsonPath("$.code", is("MAIN")))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createInstitution_negative_missingName() throws Exception {
      var body = "{\"code\":\"MAIN\"}";

      tryPost(institutionsResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")));
    }

    @Test
    void createInstitution_negative_missingCode() throws Exception {
      var body = "{\"name\":\"Main Institution\"}";

      tryPost(institutionsResource(), body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].parameters[0].key", is("code")));
    }

    @Test
    void createInstitution_negative_duplicateName() throws Exception {
      doPost(institutionsResource(), newInstitution("Unique Inst", "UNIQ1"));

      tryPost(institutionsResource(), newInstitution("Unique Inst", "UNIQ2"))
        .andExpect(status().isUnprocessableContent());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class GetInstitutionTests {

    @Test
    void getById_positive_returnsRecord() throws Exception {
      var id = createInstitution("Get Me", "GM");

      doGet(institutionResource(id))
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.name", is("Get Me")))
        .andExpect(jsonPath("$.code", is("GM")));
    }

    @Test
    void getById_negative_notFound() throws Exception {
      tryGet(institutionResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }

    @Test
    void getAll_positive_returnsAll() throws Exception {
      createInstitution("Inst A", "IA");
      createInstitution("Inst B", "IB");

      doGet(institutionsResource())
        .andExpect(jsonPath("$.totalRecords", is(2)))
        .andExpect(jsonPath("$.locinsts", hasSize(2)));
    }

    @Test
    void getAll_positive_withCqlQuery() throws Exception {
      createInstitution("Alpha Inst", "AI");
      createInstitution("Beta Inst", "BI");

      doGet(institutionsResource() + "?query=name==\"Alpha Inst\"")
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.locinsts[0].name", is("Alpha Inst")));
    }

    @Test
    void getAll_negative_invalidCql() throws Exception {
      tryGet(institutionsResource() + "?query=invalid***cql")
        .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class UpdateInstitutionTests {

    @Test
    void updateInstitution_positive_updatesRecord() throws Exception {
      var id = createInstitution("Old Name", "OLD");

      doPut(institutionResource(id), newInstitution("New Name", "NEW").id(id));

      doGet(institutionResource(id))
        .andExpect(jsonPath("$.name", is("New Name")))
        .andExpect(jsonPath("$.code", is("NEW")));
    }

    @Test
    void updateInstitution_negative_notFound() throws Exception {
      tryPut(institutionResource(UUID.randomUUID()), newInstitution("New Name", "NEW"))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class DeleteInstitutionTests {

    @Test
    void deleteById_positive_deletesRecord() throws Exception {
      var id = createInstitution("Delete Me", "DM");

      doDelete(institutionResource(id));

      tryGet(institutionResource(id))
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_negative_notFound() throws Exception {
      tryDelete(institutionResource(UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {LibraryEntity.LIBRARY_TABLE,
                             CampusEntity.CAMPUS_TABLE,
                             InstitutionEntity.INSTITUTION_TABLE}, tenants = TENANT_ID)
  class DeleteInstitutionsTests {

    @Test
    void deleteAll_positive_deletesAllRecords() throws Exception {
      createInstitution("Inst 1", "I1");
      createInstitution("Inst 2", "I2");

      doDelete(institutionsResource());

      doGet(institutionsResource())
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
