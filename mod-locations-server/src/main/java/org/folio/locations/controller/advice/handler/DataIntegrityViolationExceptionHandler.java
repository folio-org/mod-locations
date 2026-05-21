package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.ErrorCode.CONSTRAINT_VIOLATION;

import java.util.List;
import java.util.regex.Pattern;
import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.locations.domain.dto.Parameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DataIntegrityViolationExceptionHandler implements ServiceExceptionHandler {

  // Matches PostgreSQL detail: Key (lower(f_unaccent(field::text)))=(value) already exists
  private static final Pattern PG_DETAIL_PATTERN =
    Pattern.compile("Key \\(.*?([a-zA-Z_]+)(?:::\\w+)?\\)+=\\((.+?)\\) already exists");

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var detail = parseConstraintDetail(rootCause(e).getMessage());
    var message = detail != null ? detail.toMessage() : "Constraint violation.";
    var parameters = detail != null
                     ? List.of(new Parameter().key(detail.field()).value(detail.value()))
                     : List.<Parameter>of();
    var error = ServiceExceptionHandler.fromErrorCode(CONSTRAINT_VIOLATION)
      .message(message)
      .parameters(parameters);
    return ResponseEntity.status(CONSTRAINT_VIOLATION.getStatus())
      .body(ServiceExceptionHandler.errorCollection(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof DataIntegrityViolationException;
  }

  private static ConstraintDetail parseConstraintDetail(String message) {
    if (message == null) {
      return null;
    }
    var matcher = PG_DETAIL_PATTERN.matcher(message);
    return matcher.find() ? new ConstraintDetail(matcher.group(1), matcher.group(2)) : null;
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
