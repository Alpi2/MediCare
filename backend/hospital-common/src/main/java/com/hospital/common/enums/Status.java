package com.hospital.common.enums;

public enum Status {
  ACTIVE,
  INACTIVE,
  PENDING,
  SUSPENDED,
  DELETED,
  ARCHIVED;

  public boolean isActive() {
    return this == ACTIVE;
  }

  public boolean isTerminal() {
    return this == DELETED || this == ARCHIVED;
  }
}
