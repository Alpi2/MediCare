package com.hospital.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

  public ForbiddenException(String message) {
    super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
  }

  public ForbiddenException(String resource, String action) {
    super(String.format("Access denied to %s for action %s", resource, action),
        "FORBIDDEN", HttpStatus.FORBIDDEN);
  }
}
