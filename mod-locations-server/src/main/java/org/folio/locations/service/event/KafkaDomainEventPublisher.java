package org.folio.locations.service.event;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.folio.locations.domain.event.DomainEvent;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.folio.spring.tools.kafka.KafkaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

  private final FolioKafkaProperties kafkaProperties;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final Validator validator;

  @Value("${folio.environment:folio}")
  private String environment;

  /**
   * Publishes a domain event to Kafka, ensuring the message is sent only after the
   * surrounding database transaction has successfully committed.
   *
   * <p>When called within an active Spring transaction, the actual Kafka send is deferred
   * by registering an {@link TransactionSynchronization#afterCommit()} callback. This
   * guarantees that a consumer can never receive an event for a record that was ultimately
   * rolled back — which would leave consumers with a reference to data that does not exist
   * in the database.
   *
   * <p>Example of the problem this prevents:
   * <pre>
   *   BEGIN TRANSACTION
   *     INSERT INTO location ...
   *     publish("location.created") → consumer reads the event immediately
   *   ROLLBACK                       → record is gone, consumer acted on phantom data ✗
   * </pre>
   *
   * <p>With deferred publishing:
   * <pre>
   *   BEGIN TRANSACTION
   *     INSERT INTO location ...
   *     register afterCommit callback (not sent yet)
   *   COMMIT                         → record is durable and visible
   *     afterCommit fires → publish("location.created") ✓
   * </pre>
   *
   * <p>If there is no active transaction (e.g., called from a non-transactional context
   * such as a test or administrative utility), the event is sent immediately to avoid
   * silently dropping it.
   *
   * @param event the domain event to publish; must not be {@code null}
   * @param <T>   the type of the resource carried in the event payload
   */
  @Override
  public <T> void publish(DomainEvent<T> event) {
    var violations = validator.validate(event);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          send(event);
        }
      });
    } else {
      send(event);
    }
  }

  private <T> void send(DomainEvent<T> event) {
    var topic = buildTopic(event);
    var key = event.getResourceId().toString();
    log.info("Publishing domain event: type={}, resourceType={}, resourceId={}",
      event.getType(), event.getResourceType(), key);
    kafkaTemplate.send(topic, key, event);
  }

  private <T> String buildTopic(DomainEvent<T> event) {
    var resourceSegment = event.getResourceType().name().toLowerCase().replace('_', '-');
    for (var topic : kafkaProperties.getTopics()) {
      if (topic.getName().equals(resourceSegment)) {
        return KafkaUtils.getTenantTopicName(kafkaProperties.getTopicPrefix(), topic.getName(), environment,
          event.getTenant());
      }
    }
    throw new IllegalArgumentException("No topic found for resource type: " + event.getResourceType());
  }
}
