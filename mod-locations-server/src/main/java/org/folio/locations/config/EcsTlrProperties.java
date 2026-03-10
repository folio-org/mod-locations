package org.folio.locations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "folio.features.ecs-tlr")
public class EcsTlrProperties {

  /**
   * Enables the ECS TLR consortium sync feature.
   * When {@code true}, service point changes on the central tenant are propagated
   * to all consortium member tenants via Kafka.
   * Maps to the {@code ECS_TLR_FEATURE_ENABLED} environment variable.
   */
  private boolean enabled = false;
}
