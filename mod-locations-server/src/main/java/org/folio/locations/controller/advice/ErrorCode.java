package org.folio.locations.controller.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  INVALID_QUERY_VALUE("invalid_query_value", HttpStatus.UNPROCESSABLE_CONTENT),
  INVALID_REQUEST_PARAMETER("invalid_request_parameter", HttpStatus.BAD_REQUEST),
  RESOURCE_NOT_FOUND("resource_not_found", HttpStatus.NOT_FOUND),
  VALIDATION_ERROR("validation_fail", HttpStatus.UNPROCESSABLE_CONTENT),
  CONSTRAINT_VIOLATION("constraint_violation", HttpStatus.UNPROCESSABLE_CONTENT),
  BAD_REQUEST("bad_request", HttpStatus.BAD_REQUEST),
  UNEXPECTED("unexpected", HttpStatus.INTERNAL_SERVER_ERROR),

  ;

  private final String code;
  private final HttpStatus status;

  ErrorCode(String code, HttpStatus status) {
    this.code = code;
    this.status = status;
  }
}
