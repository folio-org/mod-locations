package org.folio.locations.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.locations.domain.dto.ServicePoint;
import org.folio.locations.domain.event.DomainEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import tools.jackson.core.type.TypeReference;

/**
 * Configuration for the ECS TLR sync feature.
 *
 * <p>All beans in this class — and all {@code @Service}/{@code @Component} classes
 * in the {@code consortium} package — are only created when the feature is enabled.
 * Set the {@code ECS_SERVICE_POINT_SYNC_FEATURE_ENABLED} environment variable to {@code true} to activate.
 */
@Configuration
@ConditionalOnProperty(name = EcsTlrSyncConfiguration.PROPERTY, havingValue = "true")
public class EcsTlrSyncConfiguration {

  public static final String PROPERTY = "folio.features.ecs-service-point-sync.enabled";

  @Bean
  public ConsumerFactory<String, DomainEvent<ServicePoint>> servicePointEventConsumerFactory(
    KafkaProperties kafkaProperties) {
    var props = kafkaProperties.buildConsumerProperties();
    var deserializer = new JacksonJsonDeserializer<>(new TypeReference<DomainEvent<ServicePoint>>() { });
    deserializer.addTrustedPackages("org.folio.locations.domain.*");
    deserializer.setUseTypeMapperForKey(false);
    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, DomainEvent<ServicePoint>> servicePointListenerFactory(
    ConsumerFactory<String, DomainEvent<ServicePoint>> servicePointEventConsumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, DomainEvent<ServicePoint>>();
    factory.setConsumerFactory(servicePointEventConsumerFactory);
    return factory;
  }
}
