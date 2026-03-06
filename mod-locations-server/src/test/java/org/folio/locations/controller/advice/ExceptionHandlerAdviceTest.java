package org.folio.locations.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.locations.controller.advice.handler.ServiceExceptionHandler;
import org.folio.locations.domain.dto.ErrorCollection;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ExceptionHandlerAdviceTest {

  @Mock
  private ServiceExceptionHandler handler;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(handler);
  }

  @Test
  void globalExceptionHandler_positive_delegatesToMatchingHandler() {
    var ex = new RuntimeException("test error");
    var expected = ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(new ErrorCollection());
    when(handler.canHandle(ex)).thenReturn(true);
    when(handler.handleException(ex)).thenReturn(expected);
    var advice = new ExceptionHandlerAdvice(List.of(handler));

    var response = advice.globalExceptionHandler(ex);

    assertThat(response).isEqualTo(expected);
  }

  @Test
  void globalExceptionHandler_negative_fallsBackToInternalServerError() {
    var ex = new RuntimeException("unhandled error");
    when(handler.canHandle(ex)).thenReturn(false);
    var advice = new ExceptionHandlerAdvice(List.of(handler));

    var response = advice.globalExceptionHandler(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void globalExceptionHandler_negative_noHandlersReturnsFallback() {
    var advice = new ExceptionHandlerAdvice(List.of());

    var response = advice.globalExceptionHandler(new RuntimeException("no handlers"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
