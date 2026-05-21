package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.ErrorCode.VALIDATION_ERROR;

import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.locations.exception.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ValidationExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var error = ServiceExceptionHandler.fromErrorCode(VALIDATION_ERROR)
      .message(e.getMessage());
    return ResponseEntity.status(VALIDATION_ERROR.getStatus())
      .body(ServiceExceptionHandler.errorCollection(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ValidationException;
  }
}
