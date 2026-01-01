package com.hospital.common.event;

import java.time.LocalDateTime;
import java.util.Map;

public class PatientEvent {
  private String eventId;
  private EventType eventType;
  private Long patientId;
  private String medicalRecordNumber;
  private String firstName;
  private String lastName;
  private String email;
  private LocalDateTime timestamp;
  private String source;
  private Map<String, String> metadata;
  private Integer version;

  public PatientEvent() {}

  public PatientEvent(String eventId, EventType eventType, Long patientId, String medicalRecordNumber,
      String firstName, String lastName, String email, LocalDateTime timestamp, String source,
      Map<String, String> metadata, Integer version) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.patientId = patientId;
    this.medicalRecordNumber = medicalRecordNumber;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.timestamp = timestamp;
    this.source = source;
    this.metadata = metadata;
    this.version = version;
  }

  public static Builder builder() { return new Builder(); }

  public static class Builder {
    private String eventId;
    private EventType eventType;
    private Long patientId;
    private String medicalRecordNumber;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime timestamp;
    private String source;
    private Map<String, String> metadata;
    private Integer version;

    public Builder eventId(String eventId) { this.eventId = eventId; return this; }
    public Builder eventType(EventType eventType) { this.eventType = eventType; return this; }
    public Builder patientId(Long patientId) { this.patientId = patientId; return this; }
    public Builder medicalRecordNumber(String medicalRecordNumber) { this.medicalRecordNumber = medicalRecordNumber; return this; }
    public Builder firstName(String firstName) { this.firstName = firstName; return this; }
    public Builder lastName(String lastName) { this.lastName = lastName; return this; }
    public Builder email(String email) { this.email = email; return this; }
    public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
    public Builder source(String source) { this.source = source; return this; }
    public Builder metadata(Map<String, String> metadata) { this.metadata = metadata; return this; }
    public Builder version(Integer version) { this.version = version; return this; }

    public PatientEvent build() {
      return new PatientEvent(eventId, eventType, patientId, medicalRecordNumber, firstName, lastName, email, timestamp, source, metadata, version);
    }
  }

  // getters/setters
  public String getEventId() { return eventId; }
  public void setEventId(String eventId) { this.eventId = eventId; }
  public EventType getEventType() { return eventType; }
  public void setEventType(EventType eventType) { this.eventType = eventType; }
  public Long getPatientId() { return patientId; }
  public void setPatientId(Long patientId) { this.patientId = patientId; }
  public String getMedicalRecordNumber() { return medicalRecordNumber; }
  public void setMedicalRecordNumber(String medicalRecordNumber) { this.medicalRecordNumber = medicalRecordNumber; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public LocalDateTime getTimestamp() { return timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
  public Map<String, String> getMetadata() { return metadata; }
  public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }

  public enum EventType {
    REGISTERED,
    UPDATED,
    DELETED,
    ADMITTED,
    DISCHARGED,
    TRANSFERRED
  }

  public static PatientEvent registered(Long patientId, String medicalRecordNumber,
      String firstName, String lastName, String email) {
    return PatientEvent.builder()
        .eventId(java.util.UUID.randomUUID().toString())
        .eventType(EventType.REGISTERED)
        .patientId(patientId)
        .medicalRecordNumber(medicalRecordNumber)
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .timestamp(LocalDateTime.now())
        .source("patient-service")
        .version(1)
        .build();
  }

  public static PatientEvent updated(Long patientId, String medicalRecordNumber) {
    return PatientEvent.builder()
        .eventId(java.util.UUID.randomUUID().toString())
        .eventType(EventType.UPDATED)
        .patientId(patientId)
        .medicalRecordNumber(medicalRecordNumber)
        .timestamp(LocalDateTime.now())
        .source("patient-service")
        .version(1)
        .build();
  }

  public static PatientEvent deleted(Long patientId, String medicalRecordNumber) {
    return PatientEvent.builder()
        .eventId(java.util.UUID.randomUUID().toString())
        .eventType(EventType.DELETED)
        .patientId(patientId)
        .medicalRecordNumber(medicalRecordNumber)
        .timestamp(LocalDateTime.now())
        .source("patient-service")
        .version(1)
        .build();
  }
}
