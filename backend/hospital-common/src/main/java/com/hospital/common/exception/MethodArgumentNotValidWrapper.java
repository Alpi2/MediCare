package com.hospital.common.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Small framework-agnostic wrapper to carry validation field errors into common translation utilities.
 */
public class MethodArgumentNotValidWrapper {
  private static final int BAD_REQUEST = 400;

  private int status = BAD_REQUEST;
  private List<FieldError> fieldErrors = new ArrayList<>();

  public MethodArgumentNotValidWrapper() {}

  public MethodArgumentNotValidWrapper(int status, List<FieldError> fieldErrors) {
    this.status = status;
    this.fieldErrors = fieldErrors;
  }

  public int getStatus() { return status; }
  public void setStatus(int status) { this.status = status; }
  public List<FieldError> getFieldErrors() { return fieldErrors; }
  public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }

  public static class FieldError {
    private String field;
    private String message;
    private Object rejectedValue;

    public FieldError() {}

    public FieldError(String field, String message, Object rejectedValue) {
      this.field = field;
      this.message = message;
      this.rejectedValue = rejectedValue;
    }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getRejectedValue() { return rejectedValue; }
    public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }
  }
}
