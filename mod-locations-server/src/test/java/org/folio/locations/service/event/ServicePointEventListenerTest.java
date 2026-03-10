package org.folio.locations.service.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.domain.event.DomainEventType;
import org.folio.locations.domain.type.ResourceType;
import org.folio.locations.service.consortium.ServicePointConsortiumSyncService;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.KafkaScopedExecutionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ServicePointEventListenerTest {

  @Mock
  private ServicePointConsortiumSyncService syncService;
  @Mock
  private KafkaScopedExecutionService kafkaScopedExecutionService;

  @InjectMocks
  private ServicePointEventListener listener;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(syncService, kafkaScopedExecutionService);
  }

  @Test
  void handleServicePointEvent_delegatesToSyncServiceWithinKafkaScope() {
    var event = buildEvent();
    var headers = new MessageHeaders(null);

    listener.handleServicePointEvent(event, headers);

    var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(kafkaScopedExecutionService).executeKafkaScoped(
      org.mockito.ArgumentMatchers.eq(headers), runnableCaptor.capture());

    // Execute the captured runnable to verify it calls the sync service
    runnableCaptor.getValue().run();
    verify(syncService).syncEvent(event);
  }

  private DomainEvent<ServicePoint> buildEvent() {
    return DomainEvent.<ServicePoint>builder()
      .eventId(UUID.randomUUID())
      .eventTs(System.currentTimeMillis())
      .resourceType(ResourceType.SERVICE_POINT)
      .type(DomainEventType.CREATE)
      .tenant("central")
      .resourceId(UUID.randomUUID())
      .newResource(new ServicePoint())
      .build();
  }
}
