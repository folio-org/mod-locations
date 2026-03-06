package org.folio.locations.exception;

import java.util.UUID;
import org.folio.spring.exception.NotFoundException;

public class CampusNotFoundException extends NotFoundException {

  public CampusNotFoundException(UUID id) {
    super("Campus with id [" + id + "] was not found");
  }
}
