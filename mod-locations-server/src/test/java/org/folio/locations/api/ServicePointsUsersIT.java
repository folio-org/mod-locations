package org.folio.locations.api;

import static org.folio.locations.support.ApiUrls.servicePoints;
import static org.folio.locations.support.ApiUrls.servicePointsUsers;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointsUser;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipEntity;
import org.folio.locations.domain.entity.ServicePointUserEntity;
import org.folio.locations.support.ApiUrls;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ServicePointsUsersIT extends BaseIT {

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  private static ServicePointsUser servicePointsUser(UUID userId) {
    return new ServicePointsUser(userId);
  }

  private static ServicePointsUser servicePointsUser(UUID id, UUID userId) {
    return new ServicePointsUser(userId).id(id);
  }

  @SneakyThrows
  private UUID createServicePoint() {
    var id = UUID.randomUUID();
    var name = "SP-" + id.toString().substring(0, 8);
    doPost(servicePoints(), new ServicePoint(name, name, name).id(id));
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
      var userId = UUID.randomUUID();

      doPost(servicePointsUsers(), servicePointsUser(userId))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.userId", is(userId.toString())))
        .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
    }

    @Test
    void createServicePointsUser_positive_withServicePointsIds() throws Exception {
      var userId = UUID.randomUUID();
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();

      doPost(servicePointsUsers(), servicePointsUser(userId).servicePointsIds(List.of(sp1, sp2)))
        .andExpect(jsonPath("$.servicePointsIds", hasSize(2)));
    }

    @Test
    void createServicePointsUser_positive_withDefaultServicePointId() throws Exception {
      var userId = UUID.randomUUID();
      var spId = createServicePoint();

      doPost(servicePointsUsers(),
        servicePointsUser(userId).servicePointsIds(List.of(spId)).defaultServicePointId(spId))
        .andExpect(jsonPath("$.defaultServicePointId", is(spId.toString())));
    }

    @Test
    void createServicePointsUser_negative_nonExistentDefaultServicePointId() throws Exception {
      tryPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID()).defaultServicePointId(UUID.randomUUID()))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createServicePointsUser_negative_missingUserId() throws Exception {
      tryPost(servicePointsUsers(), "{}")
        .andExpect(status().is4xxClientError());
    }

    @Test
    void createServicePointsUser_negative_duplicateUserId() throws Exception {
      var userId = UUID.randomUUID();
      doPost(servicePointsUsers(), servicePointsUser(userId));

      tryPost(servicePointsUsers(), servicePointsUser(userId))
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
      doPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID()));
      doPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID()));

      doGet(servicePointsUsers())
        .andExpect(jsonPath("$.servicePointsUsers", hasSize(2)))
        .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    void getServicePointsUsers_positive_cqlQueryByUserId() throws Exception {
      var userId = UUID.randomUUID();
      doPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID()));
      doPost(servicePointsUsers(), servicePointsUser(userId));

      doGet(servicePointsUsers() + "?query=userId==" + userId)
        .andExpect(jsonPath("$.servicePointsUsers", hasSize(1)))
        .andExpect(jsonPath("$.servicePointsUsers[0].userId", is(userId.toString())));
    }

    @Test
    void getServicePointsUsers_positive_cqlQueryByServicePointsIds() throws Exception {
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();
      var sp3 = createServicePoint();

      doPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID())
        .servicePointsIds(List.of(sp1, sp2)).defaultServicePointId(sp1));
      var expectedId = UUID.randomUUID();
      doPost(servicePointsUsers(), servicePointsUser(expectedId, UUID.randomUUID())
        .servicePointsIds(List.of(sp2, sp3)).defaultServicePointId(sp2));

      doGet(servicePointsUsers() + "?query=servicePointsIds=" + sp3)
        .andExpect(jsonPath("$.totalRecords", is(1)))
        .andExpect(jsonPath("$.servicePointsUsers[0].id", is(expectedId.toString())));
    }

    @Test
    void getServicePointsUserById_positive_returnsRecord() throws Exception {
      var id = UUID.randomUUID();
      doPost(servicePointsUsers(), servicePointsUser(id, UUID.randomUUID()));

      doGet(ApiUrls.servicePointsUser(id))
        .andExpect(jsonPath("$.id", is(id.toString())));
    }

    @Test
    void getServicePointsUserById_negative_notFound() throws Exception {
      tryGet(ApiUrls.servicePointsUser(UUID.randomUUID()))
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
      var id = UUID.randomUUID();
      var newUserId = UUID.randomUUID();
      doPost(servicePointsUsers(), servicePointsUser(id, UUID.randomUUID()));

      doPut(ApiUrls.servicePointsUser(id), servicePointsUser(id, newUserId));

      doGet(ApiUrls.servicePointsUser(id))
        .andExpect(jsonPath("$.userId", is(newUserId.toString())));
    }

    @Test
    void updateServicePointsUserById_positive_replacesServicePointsIds() throws Exception {
      var id = UUID.randomUUID();
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();
      doPost(servicePointsUsers(), servicePointsUser(id, UUID.randomUUID()).servicePointsIds(List.of(sp1, sp2)));

      var sp3 = createServicePoint();
      doPut(ApiUrls.servicePointsUser(id), servicePointsUser(id, UUID.randomUUID()).servicePointsIds(List.of(sp3)));

      doGet(ApiUrls.servicePointsUser(id))
        .andExpect(jsonPath("$.servicePointsIds", hasSize(1)))
        .andExpect(jsonPath("$.servicePointsIds[0]", is(sp3.toString())));
    }

    @Test
    void updateServicePointsUserById_positive_updatesServicePointsIds() throws Exception {
      var id = UUID.randomUUID();
      var sp1 = createServicePoint();
      var sp2 = createServicePoint();
      doPost(servicePointsUsers(), servicePointsUser(id, UUID.randomUUID()).servicePointsIds(List.of(sp1)));

      doPut(ApiUrls.servicePointsUser(id),
        servicePointsUser(id, UUID.randomUUID()).servicePointsIds(List.of(sp1, sp2)));

      doGet(ApiUrls.servicePointsUser(id))
        .andExpect(jsonPath("$.servicePointsIds", hasSize(2)));
    }

    @Test
    void updateServicePointsUserById_negative_notFound() throws Exception {
      tryPut(ApiUrls.servicePointsUser(UUID.randomUUID()), servicePointsUser(UUID.randomUUID()))
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
      var id = UUID.randomUUID();
      doPost(servicePointsUsers(), servicePointsUser(id, UUID.randomUUID()));

      doDelete(ApiUrls.servicePointsUser(id));

      tryGet(ApiUrls.servicePointsUser(id))
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteServicePointsUserById_negative_notFound() throws Exception {
      tryDelete(ApiUrls.servicePointsUser(UUID.randomUUID()))
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
      doPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID()));
      doPost(servicePointsUsers(), servicePointsUser(UUID.randomUUID()));

      doDelete(servicePointsUsers());

      doGet(servicePointsUsers())
        .andExpect(jsonPath("$.totalRecords", is(0)));
    }
  }
}
