package org.folio.locations.exception;

import java.util.UUID;
import org.folio.spring.exception.NotFoundException;

public class ServicePointNotFoundException extends NotFoundException {

  public ServicePointNotFoundException(UUID id) {
    super("Service point with id [" + id + "] was not found");
  }
}
