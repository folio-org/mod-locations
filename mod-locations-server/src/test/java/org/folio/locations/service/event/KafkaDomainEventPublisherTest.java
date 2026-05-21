package org.folio.locations.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.locations.domain.event.DomainEventType;
import org.folio.locations.domain.type.ResourceType;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaDomainEventPublisherTest {

  private static final String ENVIRONMENT = "test-env";
  private static final String TENANT_ID = "test-tenant";
  private static final UUID RESOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Mock
  private Validator validator;
  @Mock
  private FolioExecutionContext context;

  private KafkaDomainEventPublisher publisher;

  @BeforeEach
  void setUp() {
    var folioKafkaProperties = prepareFolioKafkaProperties();
    publisher = new KafkaDomainEventPublisher(folioKafkaProperties, kafkaTemplate, validator, context);
    ReflectionTestUtils.setField(publisher, "environment", ENVIRONMENT);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(kafkaTemplate, validator, context);
  }

  @Test
  @SuppressWarnings("unchecked")
  void publish_positive_withoutTransaction_sendsImmediately() {
    var event = buildEvent(ResourceType.CAMPUS, DomainEventType.CREATE);
    doReturn(Set.of()).when(validator).validate(event);
    when(context.getOkapiHeaders()).thenReturn(Map.of());

    try (var tsmMock = mockStatic(TransactionSynchronizationManager.class)) {
      tsmMock.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(false);

      publisher.publish(event);
    }

    var recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
    verify(kafkaTemplate).send(recordCaptor.capture());
    assertThat(recordCaptor.getValue().topic()).isEqualTo(ENVIRONMENT + "." + TENANT_ID + ".locations.campus");
    assertThat(recordCaptor.getValue().key()).isEqualTo(RESOURCE_ID.toString());
    assertThat(recordCaptor.getValue().value()).isEqualTo(event);
    verify(context).getOkapiHeaders();
  }

  // ── publish without active transaction ───────────────────────────────────────

  @Test
  @SuppressWarnings("unchecked")
  void publish_positive_withoutTransaction_buildsTopicFromUnderscoredResourceType() {
    var event = buildEvent(ResourceType.SERVICE_POINT_USER, DomainEventType.DELETE);
    doReturn(Set.of()).when(validator).validate(event);
    when(context.getOkapiHeaders()).thenReturn(Map.of());

    try (var tsmMock = mockStatic(TransactionSynchronizationManager.class)) {
      tsmMock.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(false);

      publisher.publish(event);
    }

    var recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
    verify(kafkaTemplate).send(recordCaptor.capture());
    assertThat(recordCaptor.getValue().topic())
      .isEqualTo(ENVIRONMENT + "." + TENANT_ID + ".locations.service-point-user");
    assertThat(recordCaptor.getValue().key()).isEqualTo(RESOURCE_ID.toString());
    assertThat(recordCaptor.getValue().value()).isEqualTo(event);
    verify(context).getOkapiHeaders();
  }

  @Test
  @SuppressWarnings("unchecked")
  void publish_negative_invalidEvent_throwsConstraintViolationException() {
    var event = buildEvent(ResourceType.CAMPUS, DomainEventType.CREATE);
    ConstraintViolation<DomainEvent<Object>> violation = mock(ConstraintViolation.class);
    doReturn(Set.of(violation)).when(validator).validate(event);

    assertThatThrownBy(() -> publisher.publish(event))
      .isInstanceOf(ConstraintViolationException.class);

    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  @SuppressWarnings("unchecked")
  void publish_positive_withinTransaction_defersUntilAfterCommit() {
    var event = buildEvent(ResourceType.CAMPUS, DomainEventType.CREATE);
    var syncCaptor = ArgumentCaptor.forClass(TransactionSynchronization.class);
    doReturn(Set.of()).when(validator).validate(event);
    when(context.getOkapiHeaders()).thenReturn(Map.of());

    try (var tsmMock = mockStatic(TransactionSynchronizationManager.class)) {
      tsmMock.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);

      publisher.publish(event);

      // The event must not be sent before the transaction commits
      verifyNoInteractions(kafkaTemplate);

      tsmMock.verify(() -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()));
      syncCaptor.getValue().afterCommit();
    }

    assertThat(syncCaptor.getValue()).isNotNull();
    var recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
    verify(kafkaTemplate).send(recordCaptor.capture());
    assertThat(recordCaptor.getValue().topic()).isEqualTo(ENVIRONMENT + "." + TENANT_ID + ".locations.campus");
    assertThat(recordCaptor.getValue().key()).isEqualTo(RESOURCE_ID.toString());
    assertThat(recordCaptor.getValue().value()).isEqualTo(event);
    verify(context).getOkapiHeaders();
  }

  // ── publish within active transaction ────────────────────────────────────────

  @Test
  void publish_positive_withinTransaction_doesNotSendOnRollback() {
    var event = buildEvent(ResourceType.CAMPUS, DomainEventType.UPDATE);
    var syncCaptor = ArgumentCaptor.forClass(TransactionSynchronization.class);
    doReturn(Set.of()).when(validator).validate(event);
    when(context.getOkapiHeaders()).thenReturn(Map.of());

    try (var tsmMock = mockStatic(TransactionSynchronizationManager.class)) {
      tsmMock.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);

      publisher.publish(event);

      verifyNoInteractions(kafkaTemplate);

      tsmMock.verify(() -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()));
      // Simulate rollback — afterCommit is NOT called, only afterCompletion with ROLLED_BACK
      syncCaptor.getValue().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    // afterCommit was never called, so Kafka should never receive a message
    verifyNoInteractions(kafkaTemplate);
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private static DomainEvent<Object> buildEvent(ResourceType resourceType, DomainEventType eventType) {
    return DomainEvent.builder()
      .eventId(UUID.randomUUID())
      .resourceType(resourceType)
      .type(eventType)
      .tenant(TENANT_ID)
      .resourceId(RESOURCE_ID)
      .build();
  }

  private FolioKafkaProperties prepareFolioKafkaProperties() {
    var folioKafkaProperties = new FolioKafkaProperties();
    folioKafkaProperties.setTopicPrefix("locations");
    var campusTopic = new FolioKafkaProperties.KafkaTopic();
    campusTopic.setName("campus");
    var spuTopic = new FolioKafkaProperties.KafkaTopic();
    spuTopic.setName("service-point-user");
    folioKafkaProperties.setTopics(List.of(
      campusTopic, spuTopic
    ));
    return folioKafkaProperties;
  }
}
