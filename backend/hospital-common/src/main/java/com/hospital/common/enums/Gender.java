package com.hospital.common.enums;

import lombok.Getter;

@Getter
public enum Gender {
  MALE("Male"),
  FEMALE("Female"),
  OTHER("Other"),
  UNKNOWN("Unknown"),
  PREFER_NOT_TO_SAY("Prefer not to say");

  private final String displayName;

  Gender(String displayName) {
    this.displayName = displayName;
  }

  public static Gender fromString(String s) {
    if (s == null) {
      return UNKNOWN;
    }
    try {
      return Gender.valueOf(s.toUpperCase().replace(' ', '_'));
    } catch (IllegalArgumentException ex) {
      return UNKNOWN;
    }
  }
}
