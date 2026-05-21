package org.folio.locations.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

  public static final String CONSORTIUM_CENTRAL_TENANT_CACHE = "consortium-central-tenant-cache";
  public static final String CONSORTIUM_TENANTS_CACHE = "consortium-tenants-cache";

  @Bean
  public CacheManager cacheManager() {
    var centralTenantCache = new CaffeineCache(CONSORTIUM_CENTRAL_TENANT_CACHE,
      Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(3600, TimeUnit.SECONDS)
        .build());

    var consortiumTenantsCache = new CaffeineCache(CONSORTIUM_TENANTS_CACHE,
      Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(3600, TimeUnit.SECONDS)
        .build());

    var manager = new SimpleCacheManager();
    manager.setCaches(List.of(centralTenantCache, consortiumTenantsCache));
    return manager;
  }
}
