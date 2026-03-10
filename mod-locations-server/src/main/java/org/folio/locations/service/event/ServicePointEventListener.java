package org.folio.locations.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.service.consortium.ServicePointConsortiumSyncService;
import org.folio.spring.tools.kafka.KafkaScopedExecutionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ServicePointEventListener {

  private final ServicePointConsortiumSyncService syncService;
  private final KafkaScopedExecutionService kafkaScopedExecutionService;

  /**
   * Consumes service point domain events from all tenant topics and propagates
   * changes to consortium member tenants when the event originates from the
   * central tenant.
   *
   * <p>Topic pattern matches: {@code {env}.{tenantId}.locations.service-point}
   */
  @KafkaListener(id = "mod-locations-service-point-listener",
                 containerFactory = "servicePointListenerFactory",
                 topicPattern = "#{folioKafkaProperties.listener['service-point'].topicPattern}",
                 groupId = "#{folioKafkaProperties.listener['service-point'].groupId}",
                 concurrency = "#{folioKafkaProperties.listener['service-point'].concurrency}")
  public void handleServicePointEvent(DomainEvent<ServicePoint> event, MessageHeaders headers) {
    log.debug("Received service point event: type={}, tenant={}, resourceId={}",
      event.getType(), event.getTenant(), event.getResourceId());
    kafkaScopedExecutionService.executeKafkaScoped(headers, () -> syncService.syncEvent(event));
  }
}
