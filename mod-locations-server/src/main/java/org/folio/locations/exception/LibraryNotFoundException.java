package org.folio.locations.exception;

import java.util.UUID;
import org.folio.spring.exception.NotFoundException;

public class LibraryNotFoundException extends NotFoundException {

  public LibraryNotFoundException(UUID id) {
    super("Library with id [" + id + "] was not found");
  }
}
