package com.hospital.common.enums;

public enum AppointmentStatus {
  SCHEDULED,
  CONFIRMED,
  CHECKED_IN,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED,
  NO_SHOW,
  RESCHEDULED;

  public boolean isFinal() {
    return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
  }

  public boolean canTransitionTo(AppointmentStatus to) {
    return this == to || switch (this) {
      case SCHEDULED -> to == CONFIRMED || to == CANCELLED || to == RESCHEDULED || to == NO_SHOW;
      case CONFIRMED -> to == CHECKED_IN || to == CANCELLED || to == RESCHEDULED || to == NO_SHOW;
      case CHECKED_IN -> to == IN_PROGRESS || to == CANCELLED;
      case IN_PROGRESS -> to == COMPLETED || to == CANCELLED;
      default -> false;
    };
  }
}
