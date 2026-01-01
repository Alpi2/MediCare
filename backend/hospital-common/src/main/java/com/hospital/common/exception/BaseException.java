package com.hospital.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
  private final String errorCode;
  private final HttpStatus httpStatus;
  private final Map<String, Object> details = new HashMap<>();

  public BaseException(String message, String errorCode, HttpStatus httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public BaseException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public String getErrorCode() { return this.errorCode; }
  public HttpStatus getHttpStatus() { return this.httpStatus; }

  public void addDetail(String key, Object value) {
    this.details.put(key, value);
  }

  public Map<String, Object> getDetails() {
    return Collections.unmodifiableMap(this.details);
  }
}
