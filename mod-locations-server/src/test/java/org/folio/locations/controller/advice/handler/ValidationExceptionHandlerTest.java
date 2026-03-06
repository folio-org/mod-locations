package org.folio.locations.controller.advice.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.locations.exception.ValidationException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@UnitTest
class ValidationExceptionHandlerTest {

  private final ValidationExceptionHandler handler = new ValidationExceptionHandler();

  @Test
  void handleException_positive_returns422WithErrorCollection() {
    var ex = new ValidationException("Hold shelf expiry period must be specified.");

    var response = handler.handleException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).hasSize(1);
    var error = body.getErrors().getFirst();
    assertThat(error.getMessage()).isEqualTo("Hold shelf expiry period must be specified.");
    assertThat(error.getCode()).isEqualTo("validation_fail");
    assertThat(error.getType()).isEqualTo("unprocessable_content");
    assertThat(error.getParameters()).isEmpty();
  }

  @Test
  void canHandle_positive_returnsTrueForValidationException() {
    assertThat(handler.canHandle(new ValidationException("msg"))).isTrue();
  }

  @Test
  void canHandle_negative_returnsFalseForOtherException() {
    assertThat(handler.canHandle(new RuntimeException("msg"))).isFalse();
  }
}
