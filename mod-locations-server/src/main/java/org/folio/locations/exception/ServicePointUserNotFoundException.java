package org.folio.locations.exception;

import java.util.UUID;
import org.folio.spring.exception.NotFoundException;

public class ServicePointUserNotFoundException extends NotFoundException {

  public ServicePointUserNotFoundException(UUID id) {
    super("Service points user with id [" + id + "] was not found");
  }
}
