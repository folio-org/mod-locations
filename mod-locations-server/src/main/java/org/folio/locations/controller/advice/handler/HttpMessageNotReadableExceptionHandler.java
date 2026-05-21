package org.folio.locations.controller.advice.handler;

import org.folio.locations.controller.advice.ErrorCode;
import org.folio.locations.domain.dto.ErrorCollection;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

@Component
public class HttpMessageNotReadableExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var errorCollection = new ErrorCollection();
    errorCollection.addErrorsItem(ServiceExceptionHandler.fromErrorCode(ErrorCode.BAD_REQUEST).message(e.getMessage()));
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof HttpMessageNotReadableException;
  }
}
