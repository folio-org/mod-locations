package org.folio.locations.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

  public static final String CONSORTIUM_CENTRAL_TENANT_CACHE = "consortium-central-tenant-cache";
  public static final String CONSORTIUM_TENANTS_CACHE = "consortium-tenants-cache";

}
