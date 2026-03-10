package org.folio.locations.service.consortium;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.domain.event.DomainEventType;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.service.crud.ServicePointService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ServicePointConsortiumSyncServiceTest {

  private static final String CENTRAL_TENANT = "central";
  private static final String MEMBER_TENANT_1 = "member1";
  private static final String MEMBER_TENANT_2 = "member2";
  private static final UUID RESOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock
  private ConsortiumTenantsService consortiumTenantsService;
  @Mock
  private ServicePointService servicePointService;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private FolioModuleMetadata moduleMetadata;

  @InjectMocks
  private ServicePointConsortiumSyncService syncService;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(consortiumTenantsService, servicePointService, context, moduleMetadata);
  }

  // ── skipping non-central tenant ──────────────────────────────────────────────

  @Test
  void syncEvent_skipsWhenNotCentralTenant() {
    when(consortiumTenantsService.isCentralTenantContext()).thenReturn(false);

    syncService.syncEvent(buildCreateEvent(new ServicePoint()));

    verifyNoInteractions(servicePointService);
    verify(consortiumTenantsService).isCentralTenantContext();
  }

  @Test
  void syncEvent_skipsWhenNoMemberTenants() {
    when(consortiumTenantsService.isCentralTenantContext()).thenReturn(true);
    when(consortiumTenantsService.getConsortiumTenants(CENTRAL_TENANT)).thenReturn(Collections.emptyList());

    syncService.syncEvent(buildCreateEvent(new ServicePoint()));

    verifyNoInteractions(servicePointService);
    verify(consortiumTenantsService).isCentralTenantContext();
    verify(consortiumTenantsService).getConsortiumTenants(CENTRAL_TENANT);
  }

  // ── CREATE propagation ────────────────────────────────────────────────────────

  @Test
  void syncEvent_propagatesCreateToAllMemberTenants() {
    var sp = new ServicePoint().name("Desk 1").code("d1");
    when(consortiumTenantsService.isCentralTenantContext()).thenReturn(true);
    when(consortiumTenantsService.getConsortiumTenants(CENTRAL_TENANT))
      .thenReturn(List.of(MEMBER_TENANT_1, MEMBER_TENANT_2));
    when(context.getOkapiHeaders()).thenReturn(Map.of());
    when(context.getFolioModuleMetadata()).thenReturn(moduleMetadata);

    try (MockedConstruction<org.folio.spring.scope.FolioExecutionContextSetter> ignored =
           Mockito.mockConstruction(org.folio.spring.scope.FolioExecutionContextSetter.class)) {
      syncService.syncEvent(buildCreateEvent(sp));
    }

    verify(servicePointService, Mockito.times(2)).create(sp);
    verify(consortiumTenantsService).isCentralTenantContext();
    verify(consortiumTenantsService).getConsortiumTenants(CENTRAL_TENANT);
    verify(context, Mockito.times(2)).getOkapiHeaders();
  }

  // ── UPDATE propagation ────────────────────────────────────────────────────────

  @Test
  void syncEvent_propagatesUpdateToAllMemberTenants() {
    var sp = new ServicePoint().name("Desk Updated").code("du");
    when(consortiumTenantsService.isCentralTenantContext()).thenReturn(true);
    when(consortiumTenantsService.getConsortiumTenants(CENTRAL_TENANT))
      .thenReturn(List.of(MEMBER_TENANT_1));
    when(context.getOkapiHeaders()).thenReturn(Map.of());
    when(context.getFolioModuleMetadata()).thenReturn(moduleMetadata);

    try (MockedConstruction<org.folio.spring.scope.FolioExecutionContextSetter> ignored =
           Mockito.mockConstruction(org.folio.spring.scope.FolioExecutionContextSetter.class)) {
      syncService.syncEvent(buildUpdateEvent(sp));
    }

    verify(servicePointService).update(RESOURCE_ID, sp);
    verify(consortiumTenantsService).isCentralTenantContext();
    verify(consortiumTenantsService).getConsortiumTenants(CENTRAL_TENANT);
    verify(context).getOkapiHeaders();
  }

  // ── DELETE propagation ────────────────────────────────────────────────────────

  @Test
  void syncEvent_propagatesDeleteToAllMemberTenants() {
    var sp = new ServicePoint().name("Desk").code("d");
    when(consortiumTenantsService.isCentralTenantContext()).thenReturn(true);
    when(consortiumTenantsService.getConsortiumTenants(CENTRAL_TENANT))
      .thenReturn(List.of(MEMBER_TENANT_1));
    when(context.getOkapiHeaders()).thenReturn(Map.of());
    when(context.getFolioModuleMetadata()).thenReturn(moduleMetadata);

    try (MockedConstruction<org.folio.spring.scope.FolioExecutionContextSetter> ignored =
           Mockito.mockConstruction(org.folio.spring.scope.FolioExecutionContextSetter.class)) {
      syncService.syncEvent(buildDeleteEvent(sp));
    }

    verify(servicePointService).deleteById(RESOURCE_ID);
    verify(consortiumTenantsService).isCentralTenantContext();
    verify(consortiumTenantsService).getConsortiumTenants(CENTRAL_TENANT);
    verify(context).getOkapiHeaders();
  }

  // ── error isolation ───────────────────────────────────────────────────────────

  @Test
  void syncEvent_continuesWithRemainingMembersWhenOneFails() {
    var sp = new ServicePoint().name("Desk 1").code("d1");
    when(consortiumTenantsService.isCentralTenantContext()).thenReturn(true);
    when(consortiumTenantsService.getConsortiumTenants(CENTRAL_TENANT))
      .thenReturn(List.of(MEMBER_TENANT_1, MEMBER_TENANT_2));
    when(context.getOkapiHeaders()).thenReturn(Map.of());
    when(context.getFolioModuleMetadata()).thenReturn(moduleMetadata);

    try (MockedConstruction<org.folio.spring.scope.FolioExecutionContextSetter> ignored =
           Mockito.mockConstruction(org.folio.spring.scope.FolioExecutionContextSetter.class)) {
      when(servicePointService.create(sp))
        .thenThrow(new RuntimeException("DB error"))
        .thenReturn(sp);

      syncService.syncEvent(buildCreateEvent(sp));
    }

    verify(servicePointService, Mockito.times(2)).create(sp);
    verify(consortiumTenantsService).isCentralTenantContext();
    verify(consortiumTenantsService).getConsortiumTenants(CENTRAL_TENANT);
    verify(context, Mockito.times(2)).getOkapiHeaders();
  }

  // ── helpers ───────────────────────────────────────────────────────────────────

  private DomainEvent<ServicePoint> buildCreateEvent(ServicePoint sp) {
    return DomainEvent.<ServicePoint>builder()
      .eventId(UUID.randomUUID())
      .eventTs(System.currentTimeMillis())
      .resourceType(ResourceType.SERVICE_POINT)
      .type(DomainEventType.CREATE)
      .tenant(CENTRAL_TENANT)
      .resourceId(RESOURCE_ID)
      .newResource(sp)
      .build();
  }

  private DomainEvent<ServicePoint> buildUpdateEvent(ServicePoint sp) {
    return DomainEvent.<ServicePoint>builder()
      .eventId(UUID.randomUUID())
      .eventTs(System.currentTimeMillis())
      .resourceType(ResourceType.SERVICE_POINT)
      .type(DomainEventType.UPDATE)
      .tenant(CENTRAL_TENANT)
      .resourceId(RESOURCE_ID)
      .newResource(sp)
      .build();
  }

  private DomainEvent<ServicePoint> buildDeleteEvent(ServicePoint sp) {
    return DomainEvent.<ServicePoint>builder()
      .eventId(UUID.randomUUID())
      .eventTs(System.currentTimeMillis())
      .resourceType(ResourceType.SERVICE_POINT)
      .type(DomainEventType.DELETE)
      .tenant(CENTRAL_TENANT)
      .resourceId(RESOURCE_ID)
      .oldResource(sp)
      .build();
  }
}
