package com.hospital.common.util;

import java.util.StringJoiner;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
  private static final int ELLIPSIS_LENGTH = 3;

  public static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  public static boolean isNotBlank(String s) {
    return !isBlank(s);
  }

  public static String capitalize(String s) {
    if (isBlank(s)) {
      return s;
    }
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }

  public static String capitalizeWords(String s) {
    if (isBlank(s)) {
      return s;
    }
    final String[] parts = s.split("\\s+");
    final StringJoiner sj = new StringJoiner(" ");
    for (final String p : parts) {
      sj.add(capitalize(p));
    }
    return sj.toString();
  }

  public static String truncate(String s, int maxLength) {
    if (s == null) {
      return null;
    }
    if (s.length() <= maxLength) {
      return s;
    }
    return s.substring(0, maxLength - ELLIPSIS_LENGTH) + "...";
  }

  public static String generateRandomString(int length) {
    return RandomStringUtils.randomAlphanumeric(length);
  }

  public static String generateMRN() {
    final String date = java.time.LocalDate.now().toString().replace("-", "");
    final String rand = RandomStringUtils.randomNumeric(4);
    return "MRN-" + date + "-" + rand;
  }

  public static String maskString(String s, int visibleStart, int visibleEnd) {
    if (s == null) {
      return null;
    }
    final int len = s.length();
    if (visibleStart + visibleEnd >= len) {
      return s;
    }
    final String start = s.substring(0, visibleStart);
    final String end = s.substring(len - visibleEnd);
    return start + "***" + end;
  }

  public static String joinNonEmpty(String delimiter, String... parts) {
    final StringJoiner sj = new StringJoiner(delimiter);
    for (final String p : parts) {
      if (isNotBlank(p)) {
        sj.add(p.trim());
      }
    }
    return sj.toString();
  }

  public static String toSnakeCase(String input) {
    if (isBlank(input)) {
      return input;
    }
    String s = input.replaceAll("[\\s-]+", "_");
    s = s.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
    s = s.replaceAll("[^A-Za-z0-9_]+", "_");
    return s.toLowerCase();
  }

  public static String toCamelCase(String input) {
    if (isBlank(input)) {
      return input;
    }
    final String[] parts = input.split("[_\\-\\s]+");
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      final String p = parts[i].toLowerCase();
      if (i == 0) {
        sb.append(p);
      } else {
        sb.append(capitalize(p));
      }
    }
    return sb.toString();
  }

  public static String removeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return s.replaceAll("\\s+", "");
  }

  public static String normalizeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return s.trim().replaceAll("\\s+", " ");
  }
}
