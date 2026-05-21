package org.folio.locations.service.consortium;

import static org.folio.locations.config.CacheConfiguration.CONSORTIUM_CENTRAL_TENANT_CACHE;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.locations.client.UserTenantsClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserTenantsService {

  private final UserTenantsClient userTenantsClient;

  /**
   * Get consortium id.
   *
   * @return consortium id if passed 'tenantId' is a part of a consortium
   **/
  public Optional<String> getConsortiumId(String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      return Optional.empty();
    }

    var userTenantsResponse = userTenantsClient.getUserTenants(tenantId);
    if (userTenantsResponse != null) {
      return userTenantsResponse.userTenants().stream()
        .filter(userTenant -> userTenant.centralTenantId().equals(tenantId))
        .findFirst()
        .map(UserTenantsClient.UserTenant::consortiumId);
    }
    return Optional.empty();
  }

  @Cacheable(cacheNames = CONSORTIUM_CENTRAL_TENANT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #tenantId")
  public Optional<String> getCentralTenant(String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      return Optional.empty();
    }

    var userTenants = userTenantsClient.getUserTenants(tenantId);
    log.debug("getCentralTenant:  tenantId: {}, response: {}", tenantId, userTenants);

    return Optional.ofNullable(userTenants)
      .map(UserTenantsClient.UserTenants::userTenants)
      .orElse(List.of())
      .stream()
      .findFirst()
      .map(UserTenantsClient.UserTenant::centralTenantId);
  }
}
