package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.ErrorCode.BAD_REQUEST;

import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.spring.cql.CqlQueryValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CqlQueryValidationExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var error = ServiceExceptionHandler.fromErrorCode(BAD_REQUEST)
      .message(e.getMessage());
    return ResponseEntity.status(BAD_REQUEST.getStatus())
      .body(ServiceExceptionHandler.errorCollection(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof CqlQueryValidationException;
  }
}
