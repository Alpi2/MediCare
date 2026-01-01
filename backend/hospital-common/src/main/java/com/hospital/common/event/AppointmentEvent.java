package com.hospital.common.event;

import java.time.LocalDateTime;
import com.hospital.common.enums.AppointmentStatus; 

public class AppointmentEvent {
  private String eventId;
  private EventType eventType;
  private Long appointmentId;
  private Long patientId;
  private Long doctorId;
  private Long departmentId;
  private LocalDateTime appointmentDateTime;
  private AppointmentStatus status;
  private Integer durationMinutes;
  private String notes;
  private LocalDateTime timestamp;
  private String source;
  private Integer version;

  public AppointmentEvent() {
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public Long getPatientId() {
    return patientId;
  }

  public void setPatientId(Long patientId) {
    this.patientId = patientId;
  }

  public Long getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(Long doctorId) {
    this.doctorId = doctorId;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(Long departmentId) {
    this.departmentId = departmentId;
  }

  public LocalDateTime getAppointmentDateTime() {
    return appointmentDateTime;
  }

  public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
    this.appointmentDateTime = appointmentDateTime;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void setStatus(AppointmentStatus status) {
    this.status = status;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public enum EventType { 
    CREATED,
    UPDATED,
    CANCELLED,
    CONFIRMED,
    CHECKED_IN,
    STARTED,
    DELETED,
    COMPLETED,
    NO_SHOW,
    RESCHEDULED
  }

  public static AppointmentEvent created(Long appointmentId, Long patientId, Long doctorId,
      LocalDateTime appointmentDateTime, Integer durationMinutes) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.CREATED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setDoctorId(doctorId);
    ev.setAppointmentDateTime(appointmentDateTime);
    ev.setStatus(AppointmentStatus.SCHEDULED);
    ev.setDurationMinutes(durationMinutes);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent cancelled(Long appointmentId, Long patientId, String notes) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.CANCELLED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setStatus(AppointmentStatus.CANCELLED);
    ev.setNotes(notes);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent confirmed(Long appointmentId, Long patientId) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.CONFIRMED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setStatus(AppointmentStatus.CONFIRMED);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent updated(Long appointmentId, Long patientId,
      Long doctorId, AppointmentStatus status) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.UPDATED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setDoctorId(doctorId);
    ev.setStatus(status);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent deleted(Long appointmentId, Long patientId) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.DELETED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setNotes("deleted");
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent checkedIn(Long appointmentId, Long patientId) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.CHECKED_IN);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setStatus(AppointmentStatus.CHECKED_IN);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent started(Long appointmentId, Long patientId) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.STARTED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setStatus(AppointmentStatus.IN_PROGRESS);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent completedEvent(Long appointmentId, Long patientId, String notes) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.COMPLETED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setStatus(AppointmentStatus.COMPLETED);
    ev.setNotes(notes);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent noShow(Long appointmentId, Long patientId) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.NO_SHOW);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setStatus(AppointmentStatus.NO_SHOW);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }

  public static AppointmentEvent rescheduled(Long appointmentId, Long patientId,
      LocalDateTime appointmentDateTime, Integer durationMinutes) {
    final AppointmentEvent ev = new AppointmentEvent();
    ev.setEventId(java.util.UUID.randomUUID().toString());
    ev.setEventType(EventType.RESCHEDULED);
    ev.setAppointmentId(appointmentId);
    ev.setPatientId(patientId);
    ev.setAppointmentDateTime(appointmentDateTime);
    ev.setDurationMinutes(durationMinutes);
    ev.setStatus(AppointmentStatus.RESCHEDULED);
    ev.setTimestamp(LocalDateTime.now());
    ev.setSource("appointment-service");
    ev.setVersion(1);
    return ev;
  }
}
