package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.handler.ServiceExceptionHandler.fromErrorCode;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.folio.locations.controller.advice.ErrorCode;
import org.folio.locations.domain.dto.Error;
import org.folio.locations.domain.dto.Parameter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConstraintViolationResolver {

  public Collection<Error> processViolation(ConstraintViolation<?> violation, ErrorCode errorCode) {
    var errors = new ArrayList<Error>();
    var propertyPath = violation.getPropertyPath();
    for (var node : propertyPath) {
      var parameter = buildParameter(node, violation);
      var message = violation.getMessage();
      var error = fromErrorCode(errorCode).message(message);
      error.addParametersItem(parameter);
      errors.add(error);
    }
    return errors;
  }

  private Parameter buildParameter(Path.Node node, ConstraintViolation<?> violation) {
    return new Parameter()
      .key(node.getName())
      .value(String.valueOf(violation.getInvalidValue()));
  }
}
