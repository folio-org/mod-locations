package org.folio.locations.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.folio.locations.LocationsApplication;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.extension.EnableKafka;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

@IntegrationTest
@EnablePostgres
@EnableKafka
@SpringBootTest(classes = LocationsApplication.class)
@AutoConfigureMockMvc
public abstract class BaseIT {

  public static final String TENANT_ID = "test_tenant";
  public static final String USER_ID = "11111111-1111-1111-1111-111111111111";

  protected static MockMvc mockMvc;
  protected static ObjectMapper objectMapper = new ObjectMapper();

  protected static void setUpTenant(String tenantId) {
    var attrs = new TenantAttributes().moduleTo("mod-locations");
    doPost("/_/tenant", tenantId, attrs);
  }

  @SneakyThrows
  protected static void removeTenant(String tenantId) {
    var attrs = new TenantAttributes().moduleFrom("mod-locations").purge(true);
    mockMvc.perform(post("/_/tenant")
        .content(asJson(attrs))
        .headers(headersForTenant(tenantId)))
      .andDo(MockMvcResultHandlers.log());
  }

  protected static HttpHeaders headersForTenant(String tenantId) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(XOkapiHeaders.TENANT, tenantId);
    headers.add(XOkapiHeaders.USER_ID, USER_ID);
    return headers;
  }

  @SneakyThrows
  protected static ResultActions tryPost(String uri, String tenantId, Object body) {
    return perform(post(uri), tenantId, body);
  }

  @SneakyThrows
  protected static ResultActions doPost(String uri, String tenantId, Object body) {
    return tryPost(uri, tenantId, body)
      .andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static ResultActions tryGet(String uri, String tenantId) {
    return perform(get(uri), tenantId, (Object) null);
  }

  @SneakyThrows
  protected static ResultActions doGet(String uri, String tenantId) {
    return tryGet(uri, tenantId)
      .andExpect(status().isOk());
  }

  @SneakyThrows
  protected static ResultActions tryPut(String uri, Object body, String tenantId) {
    return perform(put(uri), tenantId, body);
  }

  @SneakyThrows
  protected static ResultActions doPut(String uri, String tenantId, Object body) {
    return tryPut(uri, body, tenantId)
      .andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static ResultActions tryDelete(String uri, String tenantId) {
    return perform(delete(uri), tenantId, (Object) null);
  }

  @SneakyThrows
  protected static ResultActions doDelete(String uri, String tenantId) {
    return tryDelete(uri, tenantId)
      .andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static String asJson(Object value) {
    return objectMapper.writeValueAsString(value);
  }

  @BeforeAll
  static void setUpMockMvc(@Autowired MockMvc mvc) {
    mockMvc = mvc;
    setUpTenant(TENANT_ID);
  }

  @AfterAll
  static void tearDown() {
    removeTenant(TENANT_ID);
  }

  @SneakyThrows
  private static ResultActions perform(MockHttpServletRequestBuilder builder, String tenantId, Object body) {
    return perform(builder, body, headersForTenant(tenantId));
  }

  @SneakyThrows
  private static ResultActions perform(MockHttpServletRequestBuilder builder, Object body, HttpHeaders headers) {
    var content = body instanceof String s ? s : asJson(body);
    return mockMvc.perform(builder.content(content).headers(headers))
      .andDo(MockMvcResultHandlers.log());
  }
}
