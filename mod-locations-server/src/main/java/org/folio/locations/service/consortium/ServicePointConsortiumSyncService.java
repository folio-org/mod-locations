package org.folio.locations.service.consortium;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.service.crud.ServicePointService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicePointConsortiumSyncService {

  private final ConsortiumTenantsService consortiumTenantsService;
  private final ServicePointService servicePointService;
  private final FolioExecutionContext context;

  /**
   * Propagates a service point domain event to all consortium member tenants.
   *
   * <p>Only processes events originating from the central tenant — events from
   * member tenants are silently skipped to prevent infinite re-propagation.
   * Per-member failures are logged as warnings so that one unreachable tenant
   * does not block propagation to the remaining members.
   */
  public void syncEvent(DomainEvent<ServicePoint> event) {
    if (!consortiumTenantsService.isCentralTenantContext()) {
      log.debug("Skipping consortium sync for event {} - not a central tenant context", event.getResourceId());
      return;
    }

    var memberTenants = consortiumTenantsService.getConsortiumTenants(event.getTenant());
    if (memberTenants.isEmpty()) {
      return;
    }

    log.info("Syncing service point event: type={}, resourceId={}, memberTenants={}",
      event.getType(), event.getResourceId(), memberTenants.size());

    for (var memberTenantId : memberTenants) {
      try {
        syncToMember(event, memberTenantId);
      } catch (Exception e) {
        log.warn("Failed to sync service point {} to tenant {}: {}",
          event.getResourceId(), memberTenantId, e.getMessage(), e);
      }
    }
  }

  private void syncToMember(DomainEvent<ServicePoint> event, String memberTenantId) {
    Map<String, Collection<String>> memberHeaders = new HashMap<>(context.getOkapiHeaders());
    memberHeaders.put(XOkapiHeaders.TENANT, List.of(memberTenantId));

    try (var ignored = new FolioExecutionContextSetter(context.getFolioModuleMetadata(), memberHeaders)) {
      switch (event.getType()) {
        case CREATE -> servicePointService.create(requireNonNull(event.getNewResource()));
        case UPDATE -> servicePointService.update(event.getResourceId(), requireNonNull(event.getNewResource()));
        case DELETE -> servicePointService.deleteById(event.getResourceId());
        default -> log.warn("Unhandled event type for consortium sync: {}", event.getType());
      }
    }
  }
}
