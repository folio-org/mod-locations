package org.folio.locations.api;

import static org.folio.locations.service.validator.ServicePointValidator.ERR_HOLD_EXPIRY_WITHOUT_PICKUP;
import static org.folio.locations.service.validator.ServicePointValidator.ERR_PICKUP_WITHOUT_HOLD_EXPIRY;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.locations.domain.dto.HoldShelfExpiryPeriod;
import org.folio.locations.domain.dto.HoldShelfExpiryPeriod.IntervalIdEnum;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.dto.ServicePointStaffSlip;
import org.folio.locations.support.BaseIT;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DatabaseCleanup(tables = {"service_point_staff_slip", "service_point"}, tenants = "test_tenant")
class ServicePointsIT extends BaseIT {

  private static final String SERVICE_POINTS_URL = "/service-points";

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  // ── CREATE ────────────────────────────────────────────────────────────────────

  @Test
  void createServicePoint_positive_returnsCreatedWithLocation() throws Exception {
    var sp = servicePoint("Circ Desk 1", "cd1");

    tryPost(SERVICE_POINTS_URL, sp)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()))
      .andExpect(jsonPath("$.name", is("Circ Desk 1")))
      .andExpect(jsonPath("$.code", is("cd1")))
      .andExpect(jsonPath("$.metadata.createdDate", notNullValue()));
  }

  @Test
  void createServicePoint_positive_withHoldShelfExpiryPeriod() throws Exception {
    var sp = servicePoint("Circ Desk 11", "cd11")
      .pickupLocation(true)
      .holdShelfExpiryPeriod(holdShelfExpiry(3, IntervalIdEnum.MINUTES));

    tryPost(SERVICE_POINTS_URL, sp)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.holdShelfExpiryPeriod.duration", is(3)))
      .andExpect(jsonPath("$.holdShelfExpiryPeriod.intervalId", is("Minutes")));
  }

  @Test
  void createServicePoint_positive_withStaffSlips() throws Exception {
    var slipIdTrue = UUID.randomUUID().toString();
    var slipIdFalse = UUID.randomUUID().toString();
    var sp = servicePoint("Circ Desk 2", "cd2")
      .pickupLocation(true)
      .holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS))
      .addStaffSlipsItem(new ServicePointStaffSlip(UUID.fromString(slipIdTrue), true))
      .addStaffSlipsItem(new ServicePointStaffSlip(UUID.fromString(slipIdFalse), false));

    tryPost(SERVICE_POINTS_URL, sp)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.staffSlips", hasSize(2)))
      .andExpect(jsonPath("$.staffSlips[*].id", hasItem(slipIdTrue)))
      .andExpect(jsonPath("$.staffSlips[*].id", hasItem(slipIdFalse)));
  }

  @Test
  void createServicePoint_negative_missingName() throws Exception {
    var body = "{\"code\":\"cd1\",\"discoveryDisplayName\":\"Desk\"}";

    tryPost(SERVICE_POINTS_URL, body)
      .andExpect(status().isOk());
  }

  @Test
  void createServicePoint_negative_missingCode() throws Exception {
    var body = "{\"name\":\"Circ Desk 1\",\"discoveryDisplayName\":\"Desk\"}";

    tryPost(SERVICE_POINTS_URL, body)
      .andExpect(status().is4xxClientError());
  }

  @Test
  void createServicePoint_negative_missingDiscoveryDisplayName() throws Exception {
    var body = "{\"name\":\"Circ Desk 1\",\"code\":\"cd1\"}";

    tryPost(SERVICE_POINTS_URL, body)
      .andExpect(status().is4xxClientError());
  }

  @Test
  void createServicePoint_negative_duplicateName() throws Exception {
    doPost(SERVICE_POINTS_URL, servicePoint("Circ Desk Dup", "dup1"));

    tryPost(SERVICE_POINTS_URL, servicePoint("Circ Desk Dup", "dup2"))
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].code", is("constraint_violation")))
      .andExpect(jsonPath("$.errors[0].parameters[0].key", is("name")))
      .andExpect(jsonPath("$.errors[0].parameters[0].value", is("circ desk dup")));
  }

  @Test
  void createServicePoint_negative_pickupLocationWithoutHoldShelfExpiry() throws Exception {
    var sp = servicePoint("Circ Desk 3", "cd3").pickupLocation(true);

    tryPost(SERVICE_POINTS_URL, sp)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].message", is(ERR_PICKUP_WITHOUT_HOLD_EXPIRY)));
  }

  @Test
  void createServicePoint_negative_holdShelfExpiryWithoutPickupLocation() throws Exception {
    var sp = servicePoint("Circ Desk 4", "cd4")
      .pickupLocation(false)
      .holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS));

    tryPost(SERVICE_POINTS_URL, sp)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].message", is(ERR_HOLD_EXPIRY_WITHOUT_PICKUP)));
  }

  @Test
  void createServicePoint_negative_holdShelfExpiryWhenPickupLocationIsNull() throws Exception {
    var sp = servicePoint("Circ Desk 5", "cd5")
      .holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS));

    tryPost(SERVICE_POINTS_URL, sp)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors[0].message", is(ERR_HOLD_EXPIRY_WITHOUT_PICKUP)));
  }

  // ── READ ──────────────────────────────────────────────────────────────────────

  @Test
  void getServicePoints_positive_returnsAllRecords() throws Exception {
    doPost(SERVICE_POINTS_URL, servicePoint("Circ Desk A", "cda"));
    doPost(SERVICE_POINTS_URL, servicePoint("Circ Desk B", "cdb"));

    doGet(SERVICE_POINTS_URL)
      .andExpect(jsonPath("$.servicepoints", hasSize(2)))
      .andExpect(jsonPath("$.totalRecords", is(2)));
  }

  @Test
  void getServicePointById_positive_returnsRecord() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk X", "cdx"));

    doGet(SERVICE_POINTS_URL + "/" + id)
      .andExpect(jsonPath("$.id", is(id.toString())))
      .andExpect(jsonPath("$.name", is("Circ Desk X")));
  }

  @Test
  void getServicePointById_negative_notFound() throws Exception {
    tryGet(SERVICE_POINTS_URL + "/" + UUID.randomUUID())
      .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @CsvSource({
    "false, ''",
    "false, ?includeRoutingServicePoints=false",
    "true,  ?includeRoutingServicePoints=true"
  })
  void getServicePoints_ecsRoutingFilteredCorrectly(boolean includeRouting, String params) throws Exception {
    var sp1 = servicePoint(UUID.randomUUID(), "ECS Desk 1", "ecs1");
    var sp2 = servicePoint(UUID.randomUUID(), "ECS Desk 2", "ecs2");
    var sp3Id = UUID.randomUUID();
    var sp3 = servicePoint(sp3Id, "ECS Routing Desk", "ecs3")
      .pickupLocation(true)
      .holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS))
      .ecsRequestRouting(true);

    doPost(SERVICE_POINTS_URL, sp1);
    doPost(SERVICE_POINTS_URL, sp2);
    doPost(SERVICE_POINTS_URL, sp3);

    var result = doGet(SERVICE_POINTS_URL + params);
    if (includeRouting) {
      result.andExpect(jsonPath("$.servicepoints[*].id", hasItem(sp3Id.toString())))
        .andExpect(jsonPath("$.totalRecords", is(3)));
    } else {
      result.andExpect(jsonPath("$.totalRecords", is(2)));
    }
  }

  // ── UPDATE ────────────────────────────────────────────────────────────────────

  @Test
  void updateServicePoint_positive_updatesRecord() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk Old", "old1"));

    var update = servicePoint(id, "Circ Desk New", "new1");
    doPut(SERVICE_POINTS_URL + "/" + id, update);

    doGet(SERVICE_POINTS_URL + "/" + id)
      .andExpect(jsonPath("$.name", is("Circ Desk New")))
      .andExpect(jsonPath("$.code", is("new1")))
      .andExpect(jsonPath("$.metadata.updatedDate", notNullValue()));
  }

  @Test
  void updateServicePoint_positive_addHoldShelfExpiryWhenBecomingPickupLocation() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk 1", "cd1").pickupLocation(false));

    var update = servicePoint(id, "Circ Desk 2", "cd2")
      .pickupLocation(true)
      .holdShelfExpiryPeriod(holdShelfExpiry(5, IntervalIdEnum.WEEKS));
    doPut(SERVICE_POINTS_URL + "/" + id, update);

    doGet(SERVICE_POINTS_URL + "/" + id)
      .andExpect(jsonPath("$.pickupLocation", is(true)))
      .andExpect(jsonPath("$.holdShelfExpiryPeriod.duration", is(5)))
      .andExpect(jsonPath("$.holdShelfExpiryPeriod.intervalId", is("Weeks")));
  }

  @Test
  void updateServicePoint_positive_removeHoldShelfExpiryWhenNoLongerPickupLocation() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk 1", "cd1")
      .pickupLocation(true).holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS)));

    var update = servicePoint(id, "Circ Desk 2", "cd2").pickupLocation(false);
    doPut(SERVICE_POINTS_URL + "/" + id, update);

    doGet(SERVICE_POINTS_URL + "/" + id)
      .andExpect(jsonPath("$.pickupLocation", is(false)))
      .andExpect(jsonPath("$.holdShelfExpiryPeriod").doesNotExist());
  }

  @Test
  void updateServicePoint_positive_updateStaffSlips() throws Exception {
    var id = UUID.randomUUID();
    var slipId = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk 1", "cd1")
      .pickupLocation(true)
      .holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS))
      .addStaffSlipsItem(new ServicePointStaffSlip(slipId, true)));

    var update = servicePoint(id, "Circ Desk 2", "cd2")
      .pickupLocation(false)
      .addStaffSlipsItem(new ServicePointStaffSlip(slipId, false));
    doPut(SERVICE_POINTS_URL + "/" + id, update);

    doGet(SERVICE_POINTS_URL + "/" + id)
      .andExpect(jsonPath("$.staffSlips[0].id", is(slipId.toString())))
      .andExpect(jsonPath("$.staffSlips[0].printByDefault", is(false)));
  }

  @Test
  void updateServicePoint_negative_notFound() throws Exception {
    tryPut(SERVICE_POINTS_URL + "/" + UUID.randomUUID(), servicePoint("Circ Desk X", "cdx"))
      .andExpect(status().isNotFound());
  }

  @Test
  void updateServicePoint_negative_pickupLocationWithoutHoldShelfExpiry() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk 1", "cd1")
      .pickupLocation(true).holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS)));

    var update = servicePoint(id, "Circ Desk 2", "cd2").pickupLocation(true);
    tryPut(SERVICE_POINTS_URL + "/" + id, update)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors[0].message", is(ERR_PICKUP_WITHOUT_HOLD_EXPIRY)));
  }

  @Test
  void updateServicePoint_negative_holdShelfExpiryWithoutPickupLocation() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk 1", "cd1")
      .pickupLocation(true).holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS)));

    var update = servicePoint(id, "Circ Desk 2", "cd2")
      .pickupLocation(false)
      .holdShelfExpiryPeriod(holdShelfExpiry(2, IntervalIdEnum.DAYS));
    tryPut(SERVICE_POINTS_URL + "/" + id, update)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors[0].message", is(ERR_HOLD_EXPIRY_WITHOUT_PICKUP)));
  }

  // ── DELETE ────────────────────────────────────────────────────────────────────

  @Test
  void deleteServicePointById_positive_deletesRecord() throws Exception {
    var id = UUID.randomUUID();
    doPost(SERVICE_POINTS_URL, servicePoint(id, "Circ Desk Del", "del1"));

    doDelete(SERVICE_POINTS_URL + "/" + id);

    tryGet(SERVICE_POINTS_URL + "/" + id)
      .andExpect(status().isNotFound());
  }

  @Test
  void deleteServicePointById_negative_notFound() throws Exception {
    tryDelete(SERVICE_POINTS_URL + "/" + UUID.randomUUID())
      .andExpect(status().isNotFound());
  }

  // ── helpers ───────────────────────────────────────────────────────────────────

  private static ServicePoint servicePoint(String name, String code) {
    return new ServicePoint(name, code, "Display: " + name);
  }

  private static ServicePoint servicePoint(UUID id, String name, String code) {
    return servicePoint(name, code).id(id);
  }

  private static HoldShelfExpiryPeriod holdShelfExpiry(int duration, IntervalIdEnum intervalId) {
    return new HoldShelfExpiryPeriod(duration, intervalId);
  }
}
