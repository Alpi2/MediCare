package com.hospital.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private String traceId;
  private List<FieldError> errors;

  public ErrorResponse() {}

  private ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String traceId, List<FieldError> errors) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.traceId = traceId;
    this.errors = errors;
  }

  public static Builder builder() { return new Builder(); }

  public static class Builder {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;
    private List<FieldError> errors;

    public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
    public Builder status(int status) { this.status = status; return this; }
    public Builder error(String error) { this.error = error; return this; }
    public Builder message(String message) { this.message = message; return this; }
    public Builder path(String path) { this.path = path; return this; }
    public Builder traceId(String traceId) { this.traceId = traceId; return this; }
    public Builder errors(List<FieldError> errors) { this.errors = errors; return this; }

    public ErrorResponse build() {
      return new ErrorResponse(timestamp, status, error, message, path, traceId, errors);
    }
  }

  // getters/setters
  public LocalDateTime getTimestamp() { return timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
  public int getStatus() { return status; }
  public void setStatus(int status) { this.status = status; }
  public String getError() { return error; }
  public void setError(String error) { this.error = error; }
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }
  public String getTraceId() { return traceId; }
  public void setTraceId(String traceId) { this.traceId = traceId; }
  public List<FieldError> getErrors() { return errors; }
  public void setErrors(List<FieldError> errors) { this.errors = errors; }

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
