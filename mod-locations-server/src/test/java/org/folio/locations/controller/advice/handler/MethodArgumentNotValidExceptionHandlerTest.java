package org.folio.locations.controller.advice.handler;

import static org.folio.locations.controller.advice.ErrorCode.INVALID_REQUEST_PARAMETER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MethodArgumentNotValidExceptionHandlerTest {

  @Mock
  private MethodArgumentNotValidException methodArgumentNotValidException;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private MethodArgumentNotValidExceptionHandler exceptionHandler;

  @Test
  void handleException_ReturnsBadRequestWithErrorCollection() {
    // Arrange
    var field = "testField";
    var defaultMessage = "must not be null";
    var rejectedValue = "null";
    var codes = new String[] {"c1", "c2"};
    var fieldError = new FieldError("objectName", field, rejectedValue, false, codes, null, defaultMessage);
    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    // Act
    var responseEntity = exceptionHandler.handleException(methodArgumentNotValidException);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    var errorCollection = responseEntity.getBody();
    assertNotNull(errorCollection);
    assertNotNull(errorCollection.getErrors());
    assertEquals(1, errorCollection.getErrors().size());

    var error = errorCollection.getErrors().getFirst();
    assertEquals(defaultMessage, error.getMessage());
    assertEquals(INVALID_REQUEST_PARAMETER.getCode(), error.getCode());
    assertEquals(INVALID_REQUEST_PARAMETER.getStatus().name().toLowerCase(), error.getType());

    var parameter = error.getParameters().getFirst();
    assertEquals(field, parameter.getKey());
    assertEquals(rejectedValue, parameter.getValue());
  }

  @Test
  void canHandle_ReturnsTrueForMethodArgumentNotValidException() {
    // Act
    boolean canHandle = exceptionHandler.canHandle(methodArgumentNotValidException);

    // Assert
    assertTrue(canHandle);
  }

  @Test
  void canHandle_ReturnsFalseForOtherExceptions() {
    // Act
    boolean canHandle = exceptionHandler.canHandle(new Exception());

    // Assert
    assertFalse(canHandle);
  }
}
