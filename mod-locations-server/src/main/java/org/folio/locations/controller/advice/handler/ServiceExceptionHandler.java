package org.folio.locations.controller.advice.handler;

import org.folio.locations.controller.advice.ErrorCode;
import org.folio.locations.domain.dto.Error;
import org.folio.locations.domain.dto.ErrorCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface ServiceExceptionHandler {

  static ResponseEntity<ErrorCollection> fallback(Exception e) {
    ErrorCollection errorCollection = new ErrorCollection();
    errorCollection.addErrorsItem(fromErrorCode(ErrorCode.UNEXPECTED).message("Unexpected error: " + e.getMessage()));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorCollection);
  }

  static Error fromErrorCode(ErrorCode errorCode) {
    return new Error().code(errorCode.getCode()).type(errorCode.getStatus().name().toLowerCase());
  }

  static ErrorCollection errorCollection(Error... errors) {
    ErrorCollection errorCollection = new ErrorCollection();
    for (Error error : errors) {
      errorCollection.addErrorsItem(error);
    }
    return errorCollection;
  }

  ResponseEntity<ErrorCollection> handleException(Exception e);

  boolean canHandle(Exception e);
}
