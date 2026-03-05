package org.folio.locations.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.locations.exception.ValidationException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@UnitTest
class ExceptionHandlerAdviceTest {

  private static final String PG_ERROR_WRAPPED =
    "Key (lower(f_unaccent(name::text)))=(circ desk 1) already exists";
  private static final String PG_ERROR_SIMPLE =
    "Key (code)=(SP-1) already exists";

  private final ExceptionHandlerAdvice advice = new ExceptionHandlerAdvice();

  @Test
  void handleValidationException_positive_returns422WithErrorCollection() {
    var ex = new ValidationException("Hold shelf expiry period must be specified.");

    var response = advice.handleValidationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getTotalRecords()).isEqualTo(1);
    assertThat(body.getErrors()).hasSize(1);
    var error = body.getErrors().get(0);
    assertThat(error.getMessage()).isEqualTo("Hold shelf expiry period must be specified.");
    assertThat(error.getCode()).isEqualTo("validation_error");
    assertThat(error.getType()).isEqualTo("UnprocessableEntityException");
    assertThat(error.getParameters()).isEmpty();
  }

  @Test
  void handleDataIntegrityViolationException_positive_parsesWrappedFunctionDetail() {
    var cause = new RuntimeException(PG_ERROR_WRAPPED);
    var ex = new DataIntegrityViolationException("constraint violation", cause);

    var response = advice.handleDataIntegrityViolationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    var error = response.getBody().getErrors().get(0);
    assertThat(error.getMessage()).isEqualTo("'circ desk 1' is already used as 'name' and must be unique.");
    assertThat(error.getCode()).isEqualTo("constraint_violation");
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().get(0).getKey()).isEqualTo("name");
    assertThat(error.getParameters().get(0).getValue()).isEqualTo("circ desk 1");
  }

  @Test
  void handleDataIntegrityViolationException_positive_parsesSimpleColumnDetail() {
    var cause = new RuntimeException(PG_ERROR_SIMPLE);
    var ex = new DataIntegrityViolationException("constraint violation", cause);

    var response = advice.handleDataIntegrityViolationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    var error = response.getBody().getErrors().get(0);
    assertThat(error.getMessage()).isEqualTo("'SP-1' is already used as 'code' and must be unique.");
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().get(0).getKey()).isEqualTo("code");
    assertThat(error.getParameters().get(0).getValue()).isEqualTo("SP-1");
  }

  @Test
  void handleDataIntegrityViolationException_negative_unparsableDetailFallsBackToGenericMessage() {
    var cause = new RuntimeException("some unexpected db error without detail");
    var ex = new DataIntegrityViolationException("constraint violation", cause);

    var response = advice.handleDataIntegrityViolationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    var error = response.getBody().getErrors().get(0);
    assertThat(error.getMessage()).isEqualTo("Constraint violation.");
    assertThat(error.getParameters()).isEmpty();
  }

  @Test
  void handleDataIntegrityViolationException_negative_nullCauseMessageFallsBackToGenericMessage() {
    var ex = new DataIntegrityViolationException("outer message");

    var response = advice.handleDataIntegrityViolationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("Constraint violation.");
  }
}
