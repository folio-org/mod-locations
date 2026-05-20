package org.folio.locations.api;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.folio.locations.client.ConsortiumTenantsClient;
import org.folio.locations.client.ConsortiumTenantsClient.ConsortiumTenant;
import org.folio.locations.client.ConsortiumTenantsClient.ConsortiumTenants;
import org.folio.locations.client.UserTenantsClient;
import org.folio.locations.client.UserTenantsClient.UserTenant;
import org.folio.locations.client.UserTenantsClient.UserTenants;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.entity.ServicePointEntity;
import org.folio.locations.domain.entity.ServicePointStaffSlipEntity;
import org.folio.locations.support.ApiResourceUrls;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration tests for consortium service point sync.
 *
 * <p>Scenario: a service point is created/updated/deleted via the REST API in the
 * central tenant. The change is published as a Kafka domain event, consumed by
 * {@link org.folio.locations.service.event.ServicePointEventListener}, and
 * propagated to every member tenant by
 * {@link org.folio.locations.service.consortium.ServicePointConsortiumSyncService}.
 *
 * <p>The external HTTP dependencies (user-tenants, consortia) are replaced with
 * Mockito beans so no real network calls are needed. Kafka is provided by the
 * Testcontainers {@code @EnableKafka} extension inherited from {@link BaseIT}.
 */
@IntegrationTest
@TestPropertySource(properties = {
  // Enable the ECS TLR sync feature for these tests
  "folio.features.ecs-tlr.enabled=true",

  // Use a short Kafka listener poll timeout to speed up negative assertions
//  "spring.kafka.consumer.max-poll-interval=100"
})
class ServicePointConsortiumSyncIT extends BaseIT {

  /**
   * Reuse the default test tenant as the consortium central tenant.
   */
  private static final String CENTRAL_TENANT = TENANT_ID;
  private static final String MEMBER_TENANT = "member_tenant";

  private static final String CONSORTIUM_ID = "test-consortium";

  private @MockitoBean UserTenantsClient userTenantsClient;

  private @MockitoBean ConsortiumTenantsClient consortiumTenantsClient;

  private @Autowired CacheManager cacheManager;

  @BeforeAll
  static void setUpMemberTenant() {
    setUpTenant(MEMBER_TENANT);
  }

  @AfterAll
  static void tearDownMemberTenant() {
    removeTenant(MEMBER_TENANT);
  }

  /**
   * Configures mock HTTP clients so that:
   * <ul>
   *   <li>{@code CENTRAL_TENANT} is considered the consortium central tenant.</li>
   *   <li>{@code MEMBER_TENANT} belongs to the same consortium but is not central.</li>
   * </ul>
   * Mocks are reset automatically between tests by {@code @MockitoBean}.
   */
  @BeforeEach
  void setUpConsortiumMocks() {
    // Central tenant sees itself as central
    when(userTenantsClient.getUserTenants(CENTRAL_TENANT))
      .thenReturn(new UserTenants(List.of(new UserTenant(CENTRAL_TENANT, CONSORTIUM_ID))));

    // Member tenant reports central as the central tenant (prevents re-propagation)
    when(userTenantsClient.getUserTenants(MEMBER_TENANT))
      .thenReturn(new UserTenants(List.of(new UserTenant(CENTRAL_TENANT, CONSORTIUM_ID))));

    // Consortium contains one central and one member tenant
    when(consortiumTenantsClient.getConsortiumTenants(eq(CONSORTIUM_ID), anyInt()))
      .thenReturn(new ConsortiumTenants(List.of(
        new ConsortiumTenant(CENTRAL_TENANT, true),
        new ConsortiumTenant(MEMBER_TENANT, false))));
  }

  @BeforeEach
  void evictCaches() {
    cacheManager.getCacheNames().forEach(name -> requireNonNull(cacheManager.getCache(name)).clear());
  }

  // ── Create propagation ────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(
    tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
              ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = {CENTRAL_TENANT, MEMBER_TENANT})
  class CreateSyncTests {

    @Test
    void syncCreate_positive_propagatesServicePointToMemberTenant() {
      var id = UUID.randomUUID();
      var sp = new ServicePoint("Sync Desk", "sd1", "Sync Desk Display").id(id);

      doPost(ApiResourceUrls.servicePointsResource(), CENTRAL_TENANT, sp);

      await().atMost(10, SECONDS).untilAsserted(() ->
        doGet(ApiResourceUrls.servicePointResource(id), MEMBER_TENANT)
          .andExpect(jsonPath("$.id", is(id.toString())))
          .andExpect(jsonPath("$.name", is("Sync Desk")))
          .andExpect(jsonPath("$.code", is("sd1"))));
    }

    @Test
    void syncCreate_positive_propagatesPickupLocationAttributesToMemberTenant() {
      var id = UUID.randomUUID();
      var sp = new ServicePoint("Pickup Desk", "pd1", "Pickup Desk Display")
        .id(id)
        .pickupLocation(true)
        .holdShelfExpiryPeriod(new org.folio.locations.domain.dto.HoldShelfExpiryPeriod(
          3, org.folio.locations.domain.dto.HoldShelfExpiryPeriod.IntervalIdEnum.DAYS));

      doPost(ApiResourceUrls.servicePointsResource(), CENTRAL_TENANT, sp);

      await().atMost(10, SECONDS).untilAsserted(() ->
        doGet(ApiResourceUrls.servicePointResource(id), MEMBER_TENANT)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.pickupLocation", is(true)))
          .andExpect(jsonPath("$.holdShelfExpiryPeriod.duration", is(3))));
    }
  }

  // ── Update propagation ────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(
    tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
              ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = {CENTRAL_TENANT, MEMBER_TENANT})
  class UpdateSyncTests {

    @Test
    void syncUpdate_positive_propagatesUpdateToMemberTenant() {
      var id = UUID.randomUUID();
      doPost(ApiResourceUrls.servicePointsResource(),
        CENTRAL_TENANT, new ServicePoint("Desk Original", "do1", "Original Display").id(id));

      // Wait for the initial create-sync to land in member
      await().atMost(10, SECONDS).untilAsserted(() ->
        doGet(ApiResourceUrls.servicePointResource(id), MEMBER_TENANT));

      doPut(ApiResourceUrls.servicePointResource(id),
        CENTRAL_TENANT, new ServicePoint("Desk Updated", "du1", "Updated Display").id(id));

      await().atMost(10, SECONDS).untilAsserted(() ->
        doGet(ApiResourceUrls.servicePointResource(id), MEMBER_TENANT)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is("Desk Updated")))
          .andExpect(jsonPath("$.code", is("du1"))));
    }
  }

  // ── Delete propagation ────────────────────────────────────────────────────

  @Nested
  @DatabaseCleanup(
    tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
              ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = {CENTRAL_TENANT, MEMBER_TENANT})
  class DeleteSyncTests {

    @Test
    void syncDelete_positive_propagatesDeletionToMemberTenant() {
      var id = UUID.randomUUID();
      doPost(ApiResourceUrls.servicePointsResource(),
        CENTRAL_TENANT, new ServicePoint("Desk To Delete", "dtd1", "Delete Desk Display").id(id));

      // Wait for create-sync to land in member before issuing delete
      await().atMost(10, SECONDS).untilAsserted(() ->
        doGet(ApiResourceUrls.servicePointResource(id), MEMBER_TENANT));

      doDelete(ApiResourceUrls.servicePointResource(id), CENTRAL_TENANT);

      await().atMost(10, SECONDS).untilAsserted(() ->
        tryGet(ApiResourceUrls.servicePointResource(id), MEMBER_TENANT)
          .andExpect(status().isNotFound()));
    }
  }

  // ── No-op for member tenant events ───────────────────────────────────────

  @Nested
  @DatabaseCleanup(
    tables = {ServicePointStaffSlipEntity.SERVICE_POINT_STAFF_SLIP_TABLE,
              ServicePointEntity.SERVICE_POINT_TABLE},
    tenants = {CENTRAL_TENANT, MEMBER_TENANT})
  class NoSyncForMemberTenantTests {

    @Test
    void syncCreate_negative_doesNotPropagateMemberTenantEventToCentralTenant() {
      var id = UUID.randomUUID();
      var sp = new ServicePoint("Member Only Desk", "mod1", "Member Only Display").id(id);

      // Create directly in member tenant — this fires a Kafka event from MEMBER_TENANT
      doPost(ApiResourceUrls.servicePointsResource(), MEMBER_TENANT, sp);

      // Allow time for the Kafka consumer to run; assert the record never appears in central
      await().pollDelay(3, SECONDS).atMost(5, SECONDS).untilAsserted(() ->
        tryGet(ApiResourceUrls.servicePointResource(id), CENTRAL_TENANT)
          .andExpect(status().isNotFound()));
    }
  }
}
