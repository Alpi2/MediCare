package com.hospital.patient.dto;

import java.time.LocalDateTime;

import com.hospital.common.dto.AddressDto;
import com.hospital.common.enums.Gender;
import com.hospital.patient.domain.Patient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* Patient Response DTO
*
* Data transfer object for patient information responses.
* Excludes sensitive information like SSN for security.
*/
@Schema(
description = "Patient information returned by the API, including    system-generated fields and audit metadata. Read-only system fields (id, mrn    , createdDate, lastModifiedDate, createdBy, lastModifiedBy) are populated by    the server. See `BaseEntity` in hospital-common for audit field details.",
example = "{\"id\":123,\"medicalRecordNumber\":\"MRN-2024-001234\"    ,\"firstName\":\"John\",\"lastName\":\"Doe\",\"fullName\":\"John Doe\"    ,\"dateOfBirth\":\"1985-03-15\",\"age\":40,\"gender\":\"MALE\"    ,\"email\":\"john.doe@example.com\",\"phoneNumber\":\"+14155552671\"    ,\"emergencyContactName\":\"Jane Doe\"    ,\"emergencyContactPhone\":\"+14155551234\",\"address\":{\"line1\":\"123    Main St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"94105\"    ,\"country\":\"USA\"},\"bloodType\":\"O+\",\"allergies\":\"Peanuts\"    ,\"medicalConditions\":\"Hypertension\"    ,\"currentMedications\":\"Lisinopril\",\"insuranceProvider\":\"Acme Health\"    ,\"insurancePolicyNumber\":\"POL123456\",\"status\":\"ACTIVE\"    ,\"preferredLanguage\":\"EN\",\"notes\":\"NKA\"    ,\"createdDate\":\"2024-01-02T15:04:05Z\"    ,\"lastModifiedDate\":\"2024-06-10T10:20:30Z\",\"createdBy\":\"system\"    ,\"lastModifiedBy\":\"admin\",\"hasAllergies\":true    ,\"hasMedicalConditions\":true,\"isActive\":true}"
)
@SuppressWarnings("deprecation")
@Data
public class PatientResponse {
  /** Id. */

  @Schema(description = "Auto-generated unique identifier for the patient      (database PK)", example = "123", required = true, accessMode =      Schema.AccessMode.READ_ONLY)
  private Long id;
  /** Medical record number. */

  @Schema(description = "Medical Record Number (MRN) assigned by the system.      Format: MRN-YYYY-NNNNNN", example = "MRN-2024-001234", required = true,      accessMode = Schema.AccessMode.READ_ONLY)
  private String medicalRecordNumber;
  /** First name. */
  @Schema(description = "Patient's legal first name", example = "John")
  private String firstName;
  /** Middle name. */

  @Schema(description = "Patient's middle name or initial", example = "A")
  private String middleName;
  /** Last name. */

  @Schema(description = "Patient's legal family name", example = "Doe")
  private String lastName;
  /** Full name. */

  @Schema(description = "Full name (computed)", example = "John Doe", accessMode      = Schema.AccessMode.READ_ONLY)
  private String fullName;
  /** Date of birth. */

  @Schema(description = "Date of birth (ISO 8601)", example = "1985-03-15")
  private java.time.LocalDate dateOfBirth;
  /** Age. */

  @Schema(description = "Age in years (computed)", example = "40", accessMode =      Schema.AccessMode.READ_ONLY)
  private int age;
  /** Gender. */

  @Schema(description = "Patient's gender", example = "MALE", allowableValues =      {"MALE", "FEMALE", "OTHER", "UNKNOWN"})
  private Gender gender;
  /** Email. */

  @Schema(description = "Contact email", example = "john.doe@example.com")
  private String email;
  /** Phone number. */

  @Schema(description = "Contact phone number (E.164)", example =      "+14155552671")
  private String phoneNumber;
  /** Emergency contact name. */

  @Schema(description = "Emergency contact name", example = "Jane Doe")
  private String emergencyContactName;
  /** Emergency contact phone. */

  @Schema(description = "Emergency contact phone (E.164)", example =      "+14155551234")
  private String emergencyContactPhone;
  /** Emergency contact relationship. */

  @Schema(description = "Emergency contact relationship", example = "Spouse")
  private String emergencyContactRelationship;
  /** Address. */

  @Schema(description = "Residential address (see AddressDto in      hospital-common)")
  private AddressDto address;
  /** Blood type. */

  @Schema(description = "Blood type", example = "O+")
  private String bloodType;
  /** Allergies. */

  @Schema(description = "Known allergies", example = "Peanuts")
  private String allergies;
  /** Medical conditions. */

  @Schema(description = "Known medical conditions", example = "Hypertension")
  private String medicalConditions;
  /** Current medications. */

  @Schema(description = "Current medications", example = "Lisinopril")
  private String currentMedications;
  /** Insurance provider. */

  @Schema(description = "Insurance provider name", example = "Acme Health")
  private String insuranceProvider;
  /** Insurance policy number. */

  @Schema(description = "Insurance policy number", example = "POL123456")
  private String insurancePolicyNumber;
  /** Insurance group number. */

  @Schema(description = "Insurance group number", example = "GRP98765")
  private String insuranceGroupNumber;
  /** Status. */

  @Schema(description = "Patient status", example = "ACTIVE")
  private Patient.PatientStatus status;
  /** Preferred language. */

  @Schema(description = "Preferred language", example = "EN")
  private String preferredLanguage;
  /** Notes. */

  @Schema(description = "Free-text clinical or administrative notes", example =      "No known allergies")
  private String notes;
  /** Created date. */

  @Schema(description = "Timestamp when the patient was created (audit field)",      example = "2024-01-02T15:04:05Z", accessMode =      Schema.AccessMode.READ_ONLY)
  private LocalDateTime createdDate;
  /** Last modified date. */

  @Schema(description = "Timestamp when the patient was last modified (audit      field)", example = "2024-06-10T10:20:30Z", accessMode =      Schema.AccessMode.READ_ONLY)
  private LocalDateTime lastModifiedDate;
  /** Created by. */

  @Schema(description = "User who created the record (audit field) - from      BaseEntity", example = "system", accessMode = Schema.AccessMode.READ_ONLY)
  private String createdBy;
  /** Last modified by. */

  @Schema(description = "User who last modified the record (audit field) - from      BaseEntity", example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
  private String lastModifiedBy;

  // Computed fields
  /** Has allergies. */
  @Schema(description = "Indicates whether the patient has recorded allergies",      example = "true", accessMode = Schema.AccessMode.READ_ONLY)
  private boolean hasAllergies;
  /** Has medical conditions. */

  @Schema(description = "Indicates whether the patient has recorded medical      conditions", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
  private boolean hasMedicalConditions;
  /** Is active. */

  @Schema(description = "Indicates whether the patient is active (not      deactivated)", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
  private boolean isActive;

  // Explicit getters/setters used by MapStruct and when Lombok is unavailable
  public PatientResponse() { }
  public Long getId() { return this.id; }
  public void setId(Long id) { this.id = id; }

  public String getMedicalRecordNumber() { return this.medicalRecordNumber; }
  public void setMedicalRecordNumber(String medicalRecordNumber) { this.medicalRecordNumber = medicalRecordNumber; }

  public String getFirstName() { return this.firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getMiddleName() { return this.middleName; }
  public void setMiddleName(String middleName) { this.middleName = middleName; }

  public String getLastName() { return this.lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getFullName() { return this.fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }

  public java.time.LocalDate getDateOfBirth() { return this.dateOfBirth; }
  public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

  public int getAge() { return this.age; }
  public void setAge(int age) { this.age = age; }

  public com.hospital.common.enums.Gender getGender() { return this.gender; }
  public void setGender(com.hospital.common.enums.Gender gender) { this.gender = gender; }

  public String getEmail() { return this.email; }
  public void setEmail(String email) { this.email = email; }

  public String getPhoneNumber() { return this.phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

  public String getEmergencyContactName() { return this.emergencyContactName; }
  public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

  public String getEmergencyContactPhone() { return this.emergencyContactPhone; }
  public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

  public AddressDto getAddress() { return this.address; }
  public void setAddress(AddressDto address) { this.address = address; }

  public String getAllergies() { return this.allergies; }
  public void setAllergies(String allergies) { this.allergies = allergies; }

  public String getMedicalConditions() { return this.medicalConditions; }
  public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

  public String getNotes() { return this.notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public LocalDateTime getCreatedDate() { return this.createdDate; }
  public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

  public LocalDateTime getLastModifiedDate() { return this.lastModifiedDate; }
  public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

  public String getCreatedBy() { return this.createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

  public String getLastModifiedBy() { return this.lastModifiedBy; }
  public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

  public String getEmergencyContactRelationship() { return this.emergencyContactRelationship; }
  public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }

  public String getBloodType() { return this.bloodType; }
  public void setBloodType(String bloodType) { this.bloodType = bloodType; }

  public String getCurrentMedications() { return this.currentMedications; }
  public void setCurrentMedications(String currentMedications) { this.currentMedications = currentMedications; }

  public String getInsuranceProvider() { return this.insuranceProvider; }
  public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

  public String getInsurancePolicyNumber() { return this.insurancePolicyNumber; }
  public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }

  public String getInsuranceGroupNumber() { return this.insuranceGroupNumber; }
  public void setInsuranceGroupNumber(String insuranceGroupNumber) { this.insuranceGroupNumber = insuranceGroupNumber; }

  public Patient.PatientStatus getStatus() { return this.status; }
  public void setStatus(Patient.PatientStatus status) { this.status = status; }

  public String getPreferredLanguage() { return this.preferredLanguage; }
  public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

  public boolean isHasAllergies() { return this.hasAllergies; }
  public boolean getHasAllergies() { return this.hasAllergies; }
  public void setHasAllergies(boolean hasAllergies) { this.hasAllergies = hasAllergies; }

  public boolean isHasMedicalConditions() { return this.hasMedicalConditions; }
  public boolean getHasMedicalConditions() { return this.hasMedicalConditions; }
  public void setHasMedicalConditions(boolean hasMedicalConditions) { this.hasMedicalConditions = hasMedicalConditions; }

  public boolean isIsActive() { return this.isActive; }
  public boolean getIsActive() { return this.isActive; }
  public void setIsActive(boolean isActive) { this.isActive = isActive; }

  // Fallback builder used when Lombok annotation processing is not available
  public static PatientResponseBuilder builderFallbackInternal() { return new PatientResponseBuilder(); }
  // Delegate matching Lombok API
  public static PatientResponseBuilder builder() { return builderFallbackInternal(); }

  public static class PatientResponseBuilder {
    private Long id;
    private String medicalRecordNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private java.time.LocalDate dateOfBirth;
    private int age;
    private com.hospital.common.enums.Gender gender;
    private String email;
    private String phoneNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private com.hospital.common.dto.AddressDto address;
    private String bloodType;
    private String allergies;
    private String medicalConditions;
    private String currentMedications;
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private String insuranceGroupNumber;
    private com.hospital.patient.domain.Patient.PatientStatus status;
    private String preferredLanguage;
    private String notes;
    private java.time.LocalDateTime createdDate;
    private java.time.LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private boolean hasAllergies;
    private boolean hasMedicalConditions;
    private boolean isActive;

    public PatientResponseBuilder id(Long id) { this.id = id; return this; }
    public PatientResponseBuilder medicalRecordNumber(String mrn) { this.medicalRecordNumber = mrn; return this; }
    public PatientResponseBuilder firstName(String fn) { this.firstName = fn; return this; }
    public PatientResponseBuilder middleName(String mn) { this.middleName = mn; return this; }
    public PatientResponseBuilder lastName(String ln) { this.lastName = ln; return this; }
    public PatientResponseBuilder fullName(String fn) { this.fullName = fn; return this; }
    public PatientResponseBuilder dateOfBirth(java.time.LocalDate dob) { this.dateOfBirth = dob; return this; }
    public PatientResponseBuilder age(int age) { this.age = age; return this; }
    public PatientResponseBuilder gender(com.hospital.common.enums.Gender g) { this.gender = g; return this; }
    public PatientResponseBuilder email(String email) { this.email = email; return this; }
    public PatientResponseBuilder phoneNumber(String phone) { this.phoneNumber = phone; return this; }
    public PatientResponseBuilder emergencyContactName(String name) { this.emergencyContactName = name; return this; }
    public PatientResponseBuilder emergencyContactPhone(String phone) { this.emergencyContactPhone = phone; return this; }
    public PatientResponseBuilder emergencyContactRelationship(String rel) { this.emergencyContactRelationship = rel; return this; }
    public PatientResponseBuilder address(com.hospital.common.dto.AddressDto addr) { this.address = addr; return this; }
    public PatientResponseBuilder bloodType(String bt) { this.bloodType = bt; return this; }
    public PatientResponseBuilder allergies(String a) { this.allergies = a; return this; }
    public PatientResponseBuilder medicalConditions(String mc) { this.medicalConditions = mc; return this; }
    public PatientResponseBuilder currentMedications(String cm) { this.currentMedications = cm; return this; }
    public PatientResponseBuilder insuranceProvider(String ip) { this.insuranceProvider = ip; return this; }
    public PatientResponseBuilder insurancePolicyNumber(String ipn) { this.insurancePolicyNumber = ipn; return this; }
    public PatientResponseBuilder insuranceGroupNumber(String ign) { this.insuranceGroupNumber = ign; return this; }
    public PatientResponseBuilder status(com.hospital.patient.domain.Patient.PatientStatus status) { this.status = status; return this; }
    public PatientResponseBuilder preferredLanguage(String pl) { this.preferredLanguage = pl; return this; }
    public PatientResponseBuilder notes(String notes) { this.notes = notes; return this; }
    public PatientResponseBuilder createdDate(java.time.LocalDateTime cd) { this.createdDate = cd; return this; }
    public PatientResponseBuilder lastModifiedDate(java.time.LocalDateTime lmd) { this.lastModifiedDate = lmd; return this; }
    public PatientResponseBuilder createdBy(String cb) { this.createdBy = cb; return this; }
    public PatientResponseBuilder lastModifiedBy(String lb) { this.lastModifiedBy = lb; return this; }
    public PatientResponseBuilder hasAllergies(boolean b) { this.hasAllergies = b; return this; }
    public PatientResponseBuilder hasMedicalConditions(boolean b) { this.hasMedicalConditions = b; return this; }
    public PatientResponseBuilder isActive(boolean b) { this.isActive = b; return this; }

    public PatientResponse build() {
      PatientResponse p = new PatientResponse();
      p.setId(this.id);
      p.setMedicalRecordNumber(this.medicalRecordNumber);
      p.setFirstName(this.firstName);
      p.setMiddleName(this.middleName);
      p.setLastName(this.lastName);
      p.setFullName(this.fullName);
      p.setDateOfBirth(this.dateOfBirth);
      p.setGender(this.gender);
      p.setAge(this.age);
      p.setEmail(this.email);
      p.setPhoneNumber(this.phoneNumber);
      p.setEmergencyContactName(this.emergencyContactName);
      p.setEmergencyContactPhone(this.emergencyContactPhone);
      p.setEmergencyContactRelationship(this.emergencyContactRelationship);
      p.setAddress(this.address);
      p.setBloodType(this.bloodType);
      p.setAllergies(this.allergies);
      p.setMedicalConditions(this.medicalConditions);
      p.setCurrentMedications(this.currentMedications);
      p.setInsuranceProvider(this.insuranceProvider);
      p.setInsurancePolicyNumber(this.insurancePolicyNumber);
      p.setInsuranceGroupNumber(this.insuranceGroupNumber);
      p.setStatus(this.status);
      p.setPreferredLanguage(this.preferredLanguage);
      p.setNotes(this.notes);
      p.setCreatedDate(this.createdDate);
      p.setLastModifiedDate(this.lastModifiedDate);
      p.setCreatedBy(this.createdBy);
      p.setLastModifiedBy(this.lastModifiedBy);
      p.setHasAllergies(this.hasAllergies);
      p.setHasMedicalConditions(this.hasMedicalConditions);
      p.setIsActive(this.isActive);
      return p;
    }
  }
}
