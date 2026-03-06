package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.ErrorCode.RESOURCE_NOT_FOUND;

import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.spring.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class NotFoundExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var error = ServiceExceptionHandler.fromErrorCode(RESOURCE_NOT_FOUND)
      .message(e.getMessage());
    return ResponseEntity.status(RESOURCE_NOT_FOUND.getStatus())
      .body(ServiceExceptionHandler.errorCollection(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof NotFoundException;
  }
}
