package org.folio.locations.service.consortium;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.locations.client.ConsortiumTenantsClient;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ConsortiumTenantsServiceTest {

  private static final String TENANT_ID = "central-tenant";
  private static final String CONSORTIUM_ID = "consortium-1";
  private static final String MEMBER_TENANT = "member-tenant";

  @Mock
  private UserTenantsService userTenantsService;
  @Mock
  private ConsortiumTenantsClient consortiumTenantsClient;
  @Mock
  private FolioExecutionContext context;

  @InjectMocks
  private ConsortiumTenantsService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(userTenantsService, consortiumTenantsClient, context);
  }

  // ── getConsortiumTenants ──────────────────────────────────────────────────────

  @Test
  void getConsortiumTenants_positive_returnsMemberTenantsOnly() {
    var central = new ConsortiumTenantsClient.ConsortiumTenant(TENANT_ID, true);
    var member = new ConsortiumTenantsClient.ConsortiumTenant(MEMBER_TENANT, false);
    when(userTenantsService.getConsortiumId(TENANT_ID)).thenReturn(Optional.of(CONSORTIUM_ID));
    when(consortiumTenantsClient.getConsortiumTenants(CONSORTIUM_ID, 10000))
      .thenReturn(new ConsortiumTenantsClient.ConsortiumTenants(List.of(central, member)));

    var result = service.getConsortiumTenants(TENANT_ID);

    assertThat(result).containsExactly(MEMBER_TENANT);
    verify(userTenantsService).getConsortiumId(TENANT_ID);
    verify(consortiumTenantsClient).getConsortiumTenants(CONSORTIUM_ID, 10000);
  }

  @Test
  void getConsortiumTenants_positive_notInConsortium_returnsEmptyList() {
    when(userTenantsService.getConsortiumId(TENANT_ID)).thenReturn(Optional.empty());

    var result = service.getConsortiumTenants(TENANT_ID);

    assertThat(result).isEmpty();
    verify(userTenantsService).getConsortiumId(TENANT_ID);
    verifyNoInteractions(consortiumTenantsClient);
  }

  @Test
  void getConsortiumTenants_positive_httpClientError_returnsEmptyList() {
    when(userTenantsService.getConsortiumId(TENANT_ID)).thenReturn(Optional.of(CONSORTIUM_ID));
    when(consortiumTenantsClient.getConsortiumTenants(CONSORTIUM_ID, 10000))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

    var result = service.getConsortiumTenants(TENANT_ID);

    assertThat(result).isEmpty();
    verify(userTenantsService).getConsortiumId(TENANT_ID);
    verify(consortiumTenantsClient).getConsortiumTenants(CONSORTIUM_ID, 10000);
  }

  @Test
  void getConsortiumTenants_negative_unexpectedException_propagates() {
    when(userTenantsService.getConsortiumId(TENANT_ID)).thenReturn(Optional.of(CONSORTIUM_ID));
    when(consortiumTenantsClient.getConsortiumTenants(CONSORTIUM_ID, 10000))
      .thenThrow(new RuntimeException("Connection refused"));

    assertThatThrownBy(() -> service.getConsortiumTenants(TENANT_ID))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(TENANT_ID)
      .hasCauseInstanceOf(RuntimeException.class);

    verify(userTenantsService).getConsortiumId(TENANT_ID);
    verify(consortiumTenantsClient).getConsortiumTenants(CONSORTIUM_ID, 10000);
  }

  // ── isCentralTenantContext ────────────────────────────────────────────────────

  @Test
  void isCentralTenantContext_positive_currentTenantIsCentral_returnsTrue() {
    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(userTenantsService.getCentralTenant(TENANT_ID)).thenReturn(Optional.of(TENANT_ID));

    assertThat(service.isCentralTenantContext()).isTrue();

    verify(context, Mockito.times(2)).getTenantId();
    verify(userTenantsService).getCentralTenant(TENANT_ID);
  }

  @Test
  void isCentralTenantContext_negative_currentTenantIsMember_returnsFalse() {
    when(context.getTenantId()).thenReturn(MEMBER_TENANT);
    when(userTenantsService.getCentralTenant(MEMBER_TENANT)).thenReturn(Optional.of(TENANT_ID));

    assertThat(service.isCentralTenantContext()).isFalse();

    verify(context, Mockito.times(2)).getTenantId();
    verify(userTenantsService).getCentralTenant(MEMBER_TENANT);
  }

  @Test
  void isCentralTenantContext_negative_notInConsortium_returnsFalse() {
    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(userTenantsService.getCentralTenant(TENANT_ID)).thenReturn(Optional.empty());

    assertThat(service.isCentralTenantContext()).isFalse();

    verify(context).getTenantId();
    verify(userTenantsService).getCentralTenant(TENANT_ID);
  }
}
