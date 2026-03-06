package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.ErrorCode.INVALID_REQUEST_PARAMETER;

import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.locations.domain.dto.Error;
import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.locations.domain.dto.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Log4j2
@Component
@RequiredArgsConstructor
public class MethodArgumentNotValidExceptionHandler implements ServiceExceptionHandler {

  private final ConstraintViolationResolver constraintViolationResolver;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (MethodArgumentNotValidException) e;
    var errorList = new ArrayList<Error>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errorList.addAll(createErrorsFromFieldError(fieldError));
    }
    return ResponseEntity.status(INVALID_REQUEST_PARAMETER.getStatus()).body(new ErrorCollection().errors(errorList));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentNotValidException;
  }

  private Collection<Error> createErrorsFromFieldError(FieldError fieldError) {
    if (fieldError.contains(ConstraintViolation.class)) {
      var violation = fieldError.unwrap(ConstraintViolation.class);
      return constraintViolationResolver.processViolation(violation, INVALID_REQUEST_PARAMETER);
    }
    var error = ServiceExceptionHandler.fromErrorCode(INVALID_REQUEST_PARAMETER);
    error.setMessage(fieldError.getDefaultMessage());
    var parameter = new Parameter().key(fieldError.getField()).value(String.valueOf(fieldError.getRejectedValue()));
    error.addParametersItem(parameter);
    return Collections.singleton(error);
  }
}
