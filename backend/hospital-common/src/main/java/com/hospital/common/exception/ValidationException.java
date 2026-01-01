package com.hospital.common.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

  public ValidationException(String message) {
    super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
  }

  public ValidationException(String message, Map<String, String> fieldErrors) {
    super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    if (fieldErrors != null) {
      fieldErrors.forEach(this::addDetail);
    }
  }
}
