package com.hospital.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtils {
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

  public static LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now();
  }

  public static ZonedDateTime getCurrentDateTimeUTC() {
    return ZonedDateTime.now(ZoneOffset.UTC);
  }

  public static String formatDateTime(LocalDateTime dt, String pattern) {
    if (dt == null) {
      return null;
    }
    return dt.format(DateTimeFormatter.ofPattern(pattern));
  }

  public static LocalDateTime parseDateTime(String s, String pattern) {
    if (s == null) {
      return null;
    }
    return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(pattern));
  }

  public static boolean isInPast(LocalDateTime dt) {
    return dt != null && dt.isBefore(LocalDateTime.now());
  }

  public static boolean isInFuture(LocalDateTime dt) {
    return dt != null && dt.isAfter(LocalDateTime.now());
  }

  public static int calculateAge(LocalDate birthDate) {
    if (birthDate == null) {
      return 0;
    }
    return Period.between(birthDate, LocalDate.now()).getYears();
  }

  public static LocalDateTime getStartOfDay(LocalDateTime dt) {
    return dt.toLocalDate().atStartOfDay();
  }

  public static LocalDateTime getEndOfDay(LocalDateTime dt) {
    return dt.toLocalDate().atTime(LocalTime.MAX);
  }

  /**
  * Adds business days to a LocalDate (skips Saturdays and Sundays).
  * Negative days will subtract business days.
  *
  * @param start the start date
  * @param days number of business days to add (may be negative)
  * @return the resulting LocalDate or null if start is null
  */
  public static LocalDate addBusinessDays(LocalDate start, int days) {
    if (start == null) {
      return null;
    }
    if (days == 0) {
      return start;
    }
    final int step;
    if (days > 0) {
      step = 1;
    } else {
      step = -1;
    }
    LocalDate date = start;
    int remaining = Math.abs(days);
    while (remaining > 0) {
      date = date.plusDays(step);
      final DayOfWeek dow = date.getDayOfWeek();
      if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
        remaining--;
      }
    }
    return date;
  }

  /**
  * Returns true if both datetimes fall on the same calendar day (local date equality).
  *
  * @param a first datetime
  * @param b second datetime
  * @return true if both are non-null and have the same local date
  */
  public static boolean isSameDay(LocalDateTime a, LocalDateTime b) {
    if (a == null || b == null) {
      return false;
    }
    return a.toLocalDate().equals(b.toLocalDate());
  }
}
