package org.folio.locations.controller.advice.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.locations.controller.advice.ErrorCode.INVALID_QUERY_VALUE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.folio.locations.domain.dto.Error;
import org.folio.locations.domain.dto.Parameter;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class JakartaConstraintViolationExceptionHandlerTest {

  @Mock
  private ConstraintViolationResolver constraintViolationResolver;

  @InjectMocks
  private JakartaConstraintViolationExceptionHandler handler;

  @Test
  void testCanHandle() {
    assertTrue(handler.canHandle(new ConstraintViolationException(new HashSet<>())));
    assertFalse(handler.canHandle(new Exception()));
  }

  @Test
  void testHandleException() {
    final var errorMessage = "Test error message";
    final var parameter = "test";
    final var parameterValue = "TestInvalidValue";

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    var violation = mock(ConstraintViolation.class);
    violations.add(violation);
    var expectedError = new Error().message(errorMessage)
      .parameters(List.of(new Parameter().key(parameter).value(parameterValue)));
    var exception = new ConstraintViolationException("Test message", violations);
    when(constraintViolationResolver.processViolation(violation, INVALID_QUERY_VALUE))
      .thenReturn(List.of(expectedError));

    var response = handler.handleException(exception);

    assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getErrors()).isNotNull().hasSize(1);
    assertThat(response.getBody().getErrors().getFirst()).isEqualTo(expectedError);
  }
}
