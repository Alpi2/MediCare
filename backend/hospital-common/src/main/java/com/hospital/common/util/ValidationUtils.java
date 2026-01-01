package com.hospital.common.util;

import java.util.Map;
import java.util.regex.Pattern;
import com.hospital.common.exception.ValidationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {
  public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 .-]{7,20}$");
  public static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
  public static final Pattern MRN_PATTERN = Pattern.compile("^[A-Z0-9-]{3,50}$");
  public static final Pattern BLOOD_TYPE_PATTERN = Pattern.compile("^(A|B|AB|O)[+-]$");

  public static boolean isValidEmail(String s) {
    return s != null && EMAIL_PATTERN.matcher(s).matches();
  }

  public static boolean isValidPhoneNumber(String s) {
    return s != null && PHONE_PATTERN.matcher(s).matches();
  }

  public static boolean isValidSSN(String s) {
    return s != null && SSN_PATTERN.matcher(s).matches();
  }

  public static boolean isValidMRN(String s) {
    return s != null && MRN_PATTERN.matcher(s).matches();
  }

  public static boolean isValidBloodType(String s) {
    return s != null && BLOOD_TYPE_PATTERN.matcher(s).matches();
  }

  public static void validateNotEmpty(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new ValidationException(fieldName + " must not be empty");
    }
  }

  public static void validatePositive(Number value, String fieldName) {
    if (value == null || value.longValue() <= 0) {
      throw new ValidationException(fieldName + " must be positive");
    }
  }

  public static void validateRange(Number value, Number min, Number max, String fieldName) {
    if (value == null) {
      throw new ValidationException(fieldName + " must not be null");
    }
    if (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
      throw new ValidationException(fieldName + " must be between " + min + " and " + max);
    }
  }

  public static void throwValidationErrors(Map<String, String> fieldErrors) {
    throw new ValidationException("Validation failed", fieldErrors);
  }

  public static String sanitizeInput(String s) {
    if (s == null) {
      return null;
    }
    // remove control characters, normalize whitespace and strip leading/trailing
    String cleaned = s.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
    cleaned = cleaned.replaceAll("\\u0000", "");
    cleaned = cleaned.trim().replaceAll("\\s+", " ");
    return cleaned;
  }

  public static String maskSensitiveData(String s, int visible) {
    if (s == null) {
      return null;
    }
    int visibleCount = visible;
    if (visibleCount < 0) {
      visibleCount = 0;
    }
    final int len = s.length();
    if (len <= visibleCount * 2) {
      return s;
    }
    final String start = s.substring(0, visibleCount);
    final String end = s.substring(len - visibleCount);
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len - (visibleCount * 2); i++) {
      sb.append('*');
    }
    return start + sb.toString() + end;
  }

  public static boolean isValidPostalCode(String postalCode, String countryCode) {
    if (postalCode == null) {
      return false;
    }
    final String pc = postalCode.trim();
    String country = countryCode;
    if (country == null) {
      country = "";
    }
    country = country.toUpperCase();
    switch (country) {
        case "US":
        case "USA":
          return pc.matches("^\\d{5}(-\\d{4})?$");
        case "CA":
        case "CAN":
          return pc.matches("^[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d$");
        default:
          // generic: allow alphanumeric and space, 3-10 chars
          return pc.matches("^[A-Za-z0-9 ]{3,10}$");
    }
  }
}
