package org.folio.locations.api;

import static java.util.UUID.randomUUID;
import static org.folio.locations.support.ApiResourceUrls.servicePointsResource;
import static org.folio.locations.support.ApiResourceUrls.servicePointsUsersResource;
import static org.folio.locations.support.TestFactoryHelper.servicePointsUser;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipEntity;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.support.ApiResourceUrls;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@IntegrationTest
class ServicePointsUsersIT extends BaseIT {

  @BeforeAll
  static void beforeAll() {
    setUpTenant(TENANT_ID);
  }

  @SneakyThrows
  private UUID createServicePoint() {
    var id = randomUUID();
    var name = "SP-" + id.toString().substring(0, 8);
    doPost(servicePointsResource(), TENANT_ID, new ServicePoint(name, name, name).id(id));
    return id;
  }

  @Nested
  @DatabaseCleanup(tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_SERVICE_POINT_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class CreateServicePointsUserTests {

    @Test
    void createServicePointsUser_positive_returnsCreated() throws Exception {
      var userId = randomUUID();

      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(userId))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.userId", is(userId.toString())))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createServicePointsUser_positive_withServicePointsIds() throws Exception {
      var userId = randomUUID();
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();

      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(userId).servicePointsIds(List.of(sp1, sp2)))
        .andExpect(jsonPath("$.servicePointsIds", hasSize(2)));
    }

    @Test
    void createServicePointsUser_positive_withDefaultServicePointId() throws Exception {
      var userId = randomUUID();
      var spId = createServicePoint();

      doPost(servicePointsUsersResource(),
        TENANT_ID, servicePointsUser(userId).servicePointsIds(List.of(spId)).defaultServicePointId(spId))
        .andExpect(jsonPath("$.defaultServicePointId", is(spId.toString())));
    }

    @Test
    void createServicePointsUser_negative_nonExistentDefaultServicePointId() throws Exception {
      var body = servicePointsUser(randomUUID()).defaultServicePointId(randomUUID());
      tryPost(servicePointsUsersResource(), TENANT_ID, body)
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createServicePointsUser_negative_missingUserId() throws Exception {
      tryPost(servicePointsUsersResource(), TENANT_ID, "{}")
        .andExpect(status().is4xxClientError());
    }

    @Test
    void createServicePointsUser_negative_duplicateUserId() throws Exception {
      var userId = randomUUID();
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(userId));

      tryPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(userId))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors[0].code", is("constraint_violation")));
    }
  }

  @Nested
  @DatabaseCleanup(tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_SERVICE_POINT_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class GetServicePointsUsersTests {

    @Test
    void getServicePointsUsers_positive_returnsAll() throws Exception {
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(randomUUID()));
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(randomUUID()));

      doGet(servicePointsUsersResource(), TENANT_ID)
        .andExpect(jsonPath("$.servicePointsUsers", hasSize(2)))
        .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    void getServicePointsUsers_positive_cqlQueryByUserId() throws Exception {
      var userId = randomUUID();
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(randomUUID()));
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(userId));

      doGet(servicePointsUsersResource() + "?query=userId==" + userId, TENANT_ID)
        .andExpect(jsonPath("$.servicePointsUsers", hasSize(1)))
        .andExpect(jsonPath("$.servicePointsUsers[0].userId", is(userId.toString())));
    }

    @Test
    void getServicePointsUsers_positive_cqlQueryByServicePointsIds() throws Exception {
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();
      var sp3 = createServicePoint();

      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(randomUUID())
        .servicePointsIds(List.of(sp1, sp2)).defaultServicePointId(sp1));
      var expectedId = randomUUID();
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(expectedId, randomUUID())
        .servicePointsIds(List.of(sp2, sp3)).defaultServicePointId(sp2));

      doGet(servicePointsUsersResource() + "?query=servicePointsIds=" + sp3, TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.servicePointsUsers[0].id", is(expectedId.toString())));
    }

    @Test
    void getServicePointsUserById_positive_returnsRecord() throws Exception {
      var id = randomUUID();
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(id, randomUUID()));

      doGet(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID)
        .andExpect(jsonPath("$.id", is(id.toString())));
    }

    @Test
    void getServicePointsUserById_negative_notFound() throws Exception {
      tryGet(ApiResourceUrls.servicePointsUserResource(randomUUID()), TENANT_ID)
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_SERVICE_POINT_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class UpdateServicePointsUserTests {

    @Test
    void updateServicePointsUserById_positive_updatesRecord() throws Exception {
      var id = randomUUID();
      var newUserId = randomUUID();
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(id, randomUUID()));

      doPut(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID, servicePointsUser(id, newUserId));

      doGet(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID)
        .andExpect(jsonPath("$.userId", is(newUserId.toString())));
    }

    @Test
    void updateServicePointsUserById_positive_replacesServicePointsIds() throws Exception {
      var id = randomUUID();
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();
      doPost(servicePointsUsersResource(),
        TENANT_ID, servicePointsUser(id, randomUUID()).servicePointsIds(List.of(sp1, sp2)));

      var sp3 = createServicePoint();
      doPut(ApiResourceUrls.servicePointsUserResource(id),
        TENANT_ID, servicePointsUser(id, randomUUID()).servicePointsIds(List.of(sp3)));

      doGet(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID)
        .andExpect(jsonPath("$.servicePointsIds", hasSize(1)))
        .andExpect(jsonPath("$.servicePointsIds[0]", is(sp3.toString())));
    }

    @Test
    void updateServicePointsUserById_positive_updatesServicePointsIds() throws Exception {
      var id = randomUUID();
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();
      var servicePointsUser = servicePointsUser(id, randomUUID()).servicePointsIds(List.of(sp1));
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser);

      doPut(ApiResourceUrls.servicePointsUserResource(id),
        TENANT_ID, servicePointsUser(id, randomUUID()).servicePointsIds(List.of(sp1, sp2)));

      doGet(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID)
        .andExpect(jsonPath("$.servicePointsIds", hasSize(2)));
    }

    @Test
    void updateServicePointsUserById_negative_notFound() throws Exception {
      tryPut(ApiResourceUrls.servicePointsUserResource(randomUUID()), servicePointsUser(randomUUID()),
        TENANT_ID)
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_SERVICE_POINT_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class DeleteServicePointsUserTests {

    @Test
    void deleteServicePointsUserById_positive_deletesRecord() throws Exception {
      var id = randomUUID();
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(id, randomUUID()));

      doDelete(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID);

      tryGet(ApiResourceUrls.servicePointsUserResource(id), TENANT_ID)
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteServicePointsUserById_negative_notFound() throws Exception {
      tryDelete(ApiResourceUrls.servicePointsUserResource(randomUUID()), TENANT_ID)
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DatabaseCleanup(tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_SERVICE_POINT_TABLE,
                             ServicePointUserEntity.SERVICE_POINT_USER_TABLE,
                             ServicePointEntity.SERVICE_POINT_TABLE}, tenants = TENANT_ID)
  class DeleteServicePointsUsersTests {

    @Test
    void deleteServicePointsUsers_positive_deletesAll() throws Exception {
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(randomUUID()));
      doPost(servicePointsUsersResource(), TENANT_ID, servicePointsUser(randomUUID()));

      doDelete(servicePointsUsersResource(), TENANT_ID);

      doGet(servicePointsUsersResource(), TENANT_ID)
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
