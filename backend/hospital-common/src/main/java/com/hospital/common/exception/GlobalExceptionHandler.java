package com.hospital.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import com.hospital.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Framework-agnostic helper to translate known exceptions into {@link ErrorResponse} objects.
 *
 * Note: this class intentionally does not use Spring MVC annotations so the common module
 * does not pull web auto-configuration. Services that want to use these translations should
 * implement their own {@code @ControllerAdvice} / exception handlers and delegate to the
 * methods provided here.
 */
public final class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final int STATUS_BAD_REQUEST = 400;
  private static final int STATUS_FORBIDDEN = 403;
  private static final int STATUS_CONFLICT = 409;
  private static final int STATUS_INTERNAL_ERROR = 500;

  private GlobalExceptionHandler() {
  }

  private static String traceId() {
    final String tid = MDC.get("traceId");
    if (tid == null) {
      return "";
    }
    return tid;
  }

  private static ErrorResponse buildError(String error, String message, int status, String path) {
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status)
        .error(error)
        .message(message)
        .path(path)
        .traceId(traceId())
        .build();
  }

  public static ErrorResponse toErrorResponse(ResourceNotFoundException ex, HttpServletRequest req) {
    log.warn("Not found: {}", ex.getMessage());
    return buildError("Not Found", ex.getMessage(), ex.getHttpStatus().value(), req.getRequestURI());
  }

  public static ErrorResponse toErrorResponse(ValidationException ex, HttpServletRequest req) {
    log.warn("Validation failed: {}", ex.getMessage());
    return buildError("Validation Error", ex.getMessage(), ex.getHttpStatus().value(), req.getRequestURI());
  }

  public static ErrorResponse toErrorResponse(MethodArgumentNotValidWrapper ex, HttpServletRequest req) {
    final List<ErrorResponse.FieldError> fields = ex.getFieldErrors().stream()
        .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getMessage(), fe.getRejectedValue()))
        .collect(Collectors.toList());
    final ErrorResponse body = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(ex.getStatus())
        .error("Validation Failed")
        .message("One or more validation errors occurred")
        .path(req.getRequestURI())
        .traceId(traceId())
        .errors(fields)
        .build();
    log.warn("Validation errors: {}", fields);
    return body;
  }

  public static ErrorResponse toErrorResponse(DuplicateResourceException ex, HttpServletRequest req) {
    log.warn("Duplicate resource: {}", ex.getMessage());
    return buildError("Conflict", ex.getMessage(), ex.getHttpStatus().value(), req.getRequestURI());
  }

  public static ErrorResponse toErrorResponse(UnauthorizedException ex, HttpServletRequest req) {
    log.warn("Unauthorized: {}", ex.getMessage());
    return buildError("Unauthorized", ex.getMessage(), ex.getHttpStatus().value(), req.getRequestURI());
  }

  public static ErrorResponse toErrorResponse(ForbiddenException ex, HttpServletRequest req) {
    log.warn("Forbidden: {}", ex.getMessage());
    return buildError("Forbidden", ex.getMessage(), ex.getHttpStatus().value(), req.getRequestURI());
  }

  public static ErrorResponse toErrorResponseForAccessDenied(String message, HttpServletRequest req) {
    log.warn("Access denied: {}", message);
    return buildError("Access Denied", message, STATUS_FORBIDDEN, req.getRequestURI());
  }

  public static ErrorResponse toDataIntegrityError(HttpServletRequest req) {
    log.warn("Data integrity violation");
    return buildError(
      "Data Integrity Violation",
      "Constraint or unique index violated",
      STATUS_CONFLICT,
      req.getRequestURI());
  }

  public static ErrorResponse toMalformedRequest(HttpServletRequest req) {
    log.warn("Malformed request");
    return buildError("Malformed Request", "Request body could not be parsed", STATUS_BAD_REQUEST, req.getRequestURI());
  }

  public static ErrorResponse toGenericError(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);
    return buildError("Internal Error", "An unexpected error occurred", STATUS_INTERNAL_ERROR, req.getRequestURI());
  }
}

