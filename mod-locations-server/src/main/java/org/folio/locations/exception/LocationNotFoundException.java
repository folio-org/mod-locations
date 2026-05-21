package org.folio.locations.exception;

import java.util.UUID;
import org.folio.spring.exception.NotFoundException;

public class LocationNotFoundException extends NotFoundException {

  public LocationNotFoundException(UUID id) {
    super("Location with id [" + id + "] was not found");
  }
}
