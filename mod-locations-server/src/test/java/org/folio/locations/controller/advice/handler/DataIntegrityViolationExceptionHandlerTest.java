package org.folio.locations.controller.advice.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@UnitTest
class DataIntegrityViolationExceptionHandlerTest {

  private static final String PG_ERROR_WRAPPED =
    "Key (lower(f_unaccent(name::text)))=(circ desk 1) already exists";
  private static final String PG_ERROR_SIMPLE =
    "Key (code)=(SP-1) already exists";

  private final DataIntegrityViolationExceptionHandler handler =
    new DataIntegrityViolationExceptionHandler();

  @Test
  void handleException_positive_parsesWrappedFunctionDetail() {
    var cause = new RuntimeException(PG_ERROR_WRAPPED);
    var ex = new DataIntegrityViolationException("constraint violation", cause);

    var response = handler.handleException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    var error = response.getBody().getErrors().getFirst();
    assertThat(error.getMessage()).isEqualTo("'circ desk 1' is already used as 'name' and must be unique.");
    assertCodeAndType(error);
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().getFirst().getKey()).isEqualTo("name");
    assertThat(error.getParameters().getFirst().getValue()).isEqualTo("circ desk 1");
  }

  @Test
  void handleException_positive_parsesSimpleColumnDetail() {
    var cause = new RuntimeException(PG_ERROR_SIMPLE);
    var ex = new DataIntegrityViolationException("constraint violation", cause);

    var response = handler.handleException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    var error = response.getBody().getErrors().getFirst();
    assertThat(error.getMessage()).isEqualTo("'SP-1' is already used as 'code' and must be unique.");
    assertCodeAndType(error);
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().getFirst().getKey()).isEqualTo("code");
    assertThat(error.getParameters().getFirst().getValue()).isEqualTo("SP-1");
  }

  @Test
  void handleException_negative_unparsableDetailFallsBackToGenericMessage() {
    var cause = new RuntimeException("some unexpected db error without detail");
    var ex = new DataIntegrityViolationException("constraint violation", cause);

    var response = handler.handleException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    var error = response.getBody().getErrors().getFirst();
    assertThat(error.getMessage()).isEqualTo("Constraint violation.");
    assertThat(error.getCode()).isEqualTo("constraint_violation");
    assertThat(error.getType()).isEqualTo("unprocessable_content");
    assertThat(error.getParameters()).isEmpty();
  }

  @Test
  void handleException_negative_nullCauseMessageFallsBackToGenericMessage() {
    var ex = new DataIntegrityViolationException("outer message");

    var response = handler.handleException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    var error = response.getBody().getErrors().getFirst();
    assertCodeAndType(error);
    assertThat(error.getMessage()).isEqualTo("Constraint violation.");
  }

  @Test
  void canHandle_positive_returnsTrueForDataIntegrityViolationException() {
    assertThat(handler.canHandle(new DataIntegrityViolationException("msg"))).isTrue();
  }

  @Test
  void canHandle_negative_returnsFalseForOtherException() {
    assertThat(handler.canHandle(new RuntimeException("msg"))).isFalse();
  }

  private void assertCodeAndType(org.folio.locations.domain.dto.Error error) {
    assertThat(error.getCode()).isEqualTo("constraint_violation");
    assertThat(error.getType()).isEqualTo("unprocessable_content");
  }
}
