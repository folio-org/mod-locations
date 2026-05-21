package org.folio.locations.service.event;

import org.folio.locations.domain.event.DomainEvent;

public interface DomainEventPublisher {

  <T> void publish(DomainEvent<T> event);
}
