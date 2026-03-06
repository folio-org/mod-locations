package org.folio.locations.exception;

import java.util.UUID;
import org.folio.spring.exception.NotFoundException;

public class InstitutionNotFoundException extends NotFoundException {

  public InstitutionNotFoundException(UUID id) {
    super("Institution with id [" + id + "] was not found");
  }
}
