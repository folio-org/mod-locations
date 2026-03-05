package org.folio.locations.controller.advice;

import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.folio.locations.domain.dto.Error;
import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.locations.domain.dto.Parameter;
import org.folio.locations.exception.ValidationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandlerAdvice {

  // Matches PostgreSQL detail: Key (lower(f_unaccent(field::text)))=(value) already exists
  private static final Pattern PG_DETAIL_PATTERN =
    Pattern.compile("Key \\(.*?([a-zA-Z_]+)(?:::\\w+)?\\)+=\\((.+?)\\) already exists");

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorCollection> handleValidationException(ValidationException ex) {
    log.warn("Handling exception", ex);
    return errorResponse(ex.getMessage(), "validation_error", List.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorCollection> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    log.warn("Handling exception", ex);
    var detail = parseConstraintDetail(rootCause(ex).getMessage());
    var message = detail != null ? detail.toMessage() : "Constraint violation.";
    var parameters = detail != null
                     ? List.of(new Parameter().key(detail.field()).value(detail.value()))
                     : List.<Parameter>of();
    return errorResponse(message, "constraint_violation", parameters);
  }

  private static ConstraintDetail parseConstraintDetail(String message) {
    if (message == null) {
      return null;
    }
    var matcher = PG_DETAIL_PATTERN.matcher(message);
    return matcher.find() ? new ConstraintDetail(matcher.group(1), matcher.group(2)) : null;
  }

  private static ResponseEntity<ErrorCollection> errorResponse(String message, String code,
                                                               List<Parameter> parameters) {
    var error = new Error()
      .message(message)
      .type("UnprocessableEntityException")
      .code(code)
      .parameters(parameters);
    var body = new ErrorCollection()
      .addErrorsItem(error)
      .totalRecords(1);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(body);
  }

  private static Throwable rootCause(Throwable ex) {
    Throwable cause = ex.getCause();
    while (cause != null && cause != ex) {
      ex = cause;
      cause = ex.getCause();
    }
    return ex;
  }

  private record ConstraintDetail(String field, String value) {
    String toMessage() {
      return "'" + value + "' is already used as '" + field + "' and must be unique.";
    }
  }
}
