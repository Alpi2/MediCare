package com.hospital.patient.dto;

import com.hospital.patient.domain.Patient;
import com.hospital.common.enums.Gender;
import com.hospital.common.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
* Patient Update Request DTO
*
* Data transfer object for updating existing patients.
* Similar to create request but allows partial updates.
*/
@Schema(
description = "Request payload for updating existing patient information. All    fields are optional - only provided fields will be updated. Immutable fields    such as SSN and MRN cannot be changed via this endpoint.",
example = "{\"email\":\"updated.email@example.com\"    ,\"phoneNumber\":\"+14155559876\",\"address\":{\"line1\":\"456 Elm St\"    ,\"city\":\"Othertown\"}}"
)
@SuppressWarnings("deprecation")
@Data
@Builder
public class PatientUpdateRequest {
  /** First name. */

  @Size(min = 1, max = 100, message = "First name must be between 1 and 100      characters")
  @Schema(description = "Patient's legal first name as it appears on      identification documents. Optional on update.", example = "John", required      = false, minLength = 1, maxLength = 100)
  private String firstName;
  /** Middle name. */

  @Size(max = 100, message = "Middle name cannot exceed 100 characters")
  @Schema(description = "Patient's middle name or initial. Optional on update.",      example = "A", required = false, maxLength = 100)
  private String middleName;
  /** Last name. */

  @Size(min = 1, max = 100, message = "Last name must be between 1 and 100      characters")
  @Schema(description = "Patient's legal family name. Optional on update.",      example = "Doe", required = false, minLength = 1, maxLength = 100)
  private String lastName;
  /** Date of birth. */

  @Past(message = "Date of birth must be in the past")
  @Schema(description = "Date of birth in ISO 8601 format (YYYY-MM-DD). Optional      on update; typically immutable but allowed for correction in rare cases.",      example = "1985-03-15", required = false, pattern =      "^\\d{4}-\\d{2}-\\d{2}$")
  private LocalDate dateOfBirth;
  /** Gender. */

  @Schema(description = "Patient's gender. Optional on update.", example =      "FEMALE", required = false, allowableValues = {"MALE", "FEMALE", "OTHER",      "UNKNOWN"})
  private Gender gender;
  /** Email. */

  @Email(message = "Invalid email format")
  @Size(max = 255, message = "Email cannot exceed 255 characters")
  @Schema(description = "Contact email address. Optional on update. Validation:      @Email, @Size(max=255)", example = "updated.email@example.com", required =      false, maxLength = 255)
  private String email;
  /** Phone number. */

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number      format")
  @Schema(description = "Contact phone number in E.164 format. Optional on      update.", example = "+14155559876", required = false, pattern =      "^\\+?[1-9]\\d{1,14}$")
  private String phoneNumber;
  /** Emergency contact name. */

  @Size(max = 200, message = "Emergency contact name cannot exceed 200      characters")
  @Schema(description = "Emergency contact person name. Optional on update.",      example = "Jane Doe", required = false, maxLength = 200)
  private String emergencyContactName;
  /** Emergency contact phone. */

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid emergency contact      phone format")
  @Schema(description = "Emergency contact phone in E.164 format. Optional on      update.", example = "+14155551234", required = false, pattern =      "^\\+?[1-9]\\d{1,14}$")
  private String emergencyContactPhone;
  /** Emergency contact relationship. */

  @Size(max = 50, message = "Emergency contact relationship cannot exceed 50      characters")
  @Schema(description = "Relationship of emergency contact to patient. Optional      on update.", example = "Spouse", required = false, maxLength = 50)
  private String emergencyContactRelationship;
  /** Address. */

  @Valid
  @Schema(description = "Residential address. See `AddressDto` in      hospital-common for field definitions and constraints. Optional on      update.", required = false)
  private AddressDto address;
  /** Blood type. */

  @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type format")
  @Schema(description = "Blood type (A, B, AB, O with + or -). Optional on      update.", example = "A-", required = false, pattern = "^(A|B|AB|O)[+-]$")
  private String bloodType;
  /** Allergies. */

  @Schema(description = "Known allergies (free text). Optional on update.",      example = "Penicillin", required = false)
  private String allergies;
  /** Medical conditions. */

  @Schema(description = "Known medical conditions (free text). Optional on      update.", example = "Asthma", required = false)
  private String medicalConditions;
  /** Current medications. */

  @Schema(description = "Current medications (free text). Optional on update.",      example = "Albuterol inhaler as needed", required = false)
  private String currentMedications;
  /** Insurance provider. */

  @Size(max = 200, message = "Insurance provider cannot exceed 200 characters")
  @Schema(description = "Primary insurance provider name. Optional on update.",      example = "Acme Health", required = false, maxLength = 200)
  private String insuranceProvider;
  /** Insurance policy number. */

  @Size(max = 100, message = "Insurance policy number cannot exceed 100      characters")
  @Schema(description = "Insurance policy number. Optional on update.", example      = "POL654321", required = false, maxLength = 100)
  private String insurancePolicyNumber;
  /** Insurance group number. */

  @Size(max = 100, message = "Insurance group number cannot exceed 100      characters")
  @Schema(description = "Insurance group number. Optional on update.", example =      "GRP55555", required = false, maxLength = 100)
  private String insuranceGroupNumber;
  /** Status. */

  @Schema(description = "Patient status (e.g., ACTIVE, INACTIVE). Optional on      update.", example = "ACTIVE", required = false)
  private Patient.PatientStatus status;
  /** Preferred language. */

  @Size(max = 50, message = "Preferred language cannot exceed 50 characters")
  @Schema(description = "Preferred communication language (ISO language code      recommended). Optional on update.", example = "EN", required = false,      maxLength = 50)
  private String preferredLanguage;
  /** Notes. */

  @Schema(description = "Free-text clinical or administrative notes. Optional on      update.", example = "Updated emergency contact", required = false)
  private String notes;

  // No-arg constructor for frameworks and fallback builders
  public PatientUpdateRequest() { }

  // Explicit getters (Lombok not processed in this module)
  public String getFirstName() { return this.firstName; }
  public String getMiddleName() { return this.middleName; }
  public String getLastName() { return this.lastName; }
  public java.time.LocalDate getDateOfBirth() { return this.dateOfBirth; }
  public com.hospital.patient.domain.Patient.PatientStatus getStatus() { return this.status; }
  public com.hospital.common.enums.Gender getGender() { return this.gender; }
  public String getEmail() { return this.email; }
  public String getPhoneNumber() { return this.phoneNumber; }
  public String getEmergencyContactName() { return this.emergencyContactName; }
  public String getEmergencyContactPhone() { return this.emergencyContactPhone; }
  public com.hospital.common.dto.AddressDto getAddress() { return this.address; }
  public String getBloodType() { return this.bloodType; }
  public String getAllergies() { return this.allergies; }
  public String getMedicalConditions() { return this.medicalConditions; }
  public String getCurrentMedications() { return this.currentMedications; }
  public String getInsuranceProvider() { return this.insuranceProvider; }
  public String getInsurancePolicyNumber() { return this.insurancePolicyNumber; }
  public String getInsuranceGroupNumber() { return this.insuranceGroupNumber; }
  public com.hospital.patient.domain.Patient.PatientStatus getStatusField() { return this.status; }
  public String getPreferredLanguage() { return this.preferredLanguage; }
  public String getNotes() { return this.notes; }

  // Fallback builder used when Lombok annotation processing is not available
  public static PatientUpdateRequestBuilder builderFallbackInternal() { return new PatientUpdateRequestBuilder(); }
  // Delegate matching Lombok API
  public static PatientUpdateRequestBuilder builder() { return builderFallbackInternal(); }

  public static class PatientUpdateRequestBuilder {
    private String firstName;
    private String lastName;
    private String email;

    public PatientUpdateRequestBuilder firstName(String fn) { this.firstName = fn; return this; }
    public PatientUpdateRequestBuilder lastName(String ln) { this.lastName = ln; return this; }
    public PatientUpdateRequestBuilder email(String email) { this.email = email; return this; }

    public PatientUpdateRequest build() {
      PatientUpdateRequest r = new PatientUpdateRequest();
      r.firstName = this.firstName;
      r.lastName = this.lastName;
      r.email = this.email;
      return r;
    }
  }
}
