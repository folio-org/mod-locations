package org.folio.locations.controller.advice;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.locations.controller.advice.handler.ServiceExceptionHandler;
import org.folio.locations.domain.dto.ErrorCollection;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ExceptionHandlerAdvice {

  private final List<ServiceExceptionHandler> exceptionHandlers;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorCollection> globalExceptionHandler(Exception e) {
    log.debug("Trying to handle [exception: {}}, message: {}]", e.getClass().getName(), e.getMessage());
    for (ServiceExceptionHandler exceptionHandler : exceptionHandlers) {
      if (exceptionHandler.canHandle(e)) {
        return exceptionHandler.handleException(e);
      }
    }
    log.error("Failed to handle exception", e);
    return ServiceExceptionHandler.fallback(e);
  }
}
