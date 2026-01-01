package com.hospital.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

  public UnauthorizedException() {
    super("Authentication required", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
  }

  public UnauthorizedException(String message) {
    super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
  }
}
