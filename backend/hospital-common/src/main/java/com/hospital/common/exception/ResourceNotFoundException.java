package com.hospital.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

  public ResourceNotFoundException(String message) {
    super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
  }

  public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
    super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
        "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    addDetail(fieldName, fieldValue);
  }
}
