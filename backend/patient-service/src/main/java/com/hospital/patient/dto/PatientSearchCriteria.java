package com.hospital.patient.dto;

import com.hospital.patient.domain.Patient;
import com.hospital.common.enums.Gender;
import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
* Patient Search Criteria DTO
*
* Data transfer object for advanced patient search operations.
* Supports multiple search parameters and filters.
*/
@Schema(
description = "Search criteria for filtering patients. All fields are optional    and combined with AND logic.",
example = "{\"firstName\":\"Jo\",\"lastName\":\"Doe\"    ,\"dateOfBirthFrom\":\"1970-01-01\",\"dateOfBirthTo\":\"2000-12-31\"    ,\"gender\":\"FEMALE\",\"insuranceProvider\":\"Acme\"}"
)
@SuppressWarnings("deprecation")
@Data
@Builder
public class PatientSearchCriteria {
  /** First name. */

  @Schema(description = "Patient first name (partial match, case-insensitive).      Use partial values to match beginning or substring.", example = "Jo",      required = false)
  private String firstName;
  /** Last name. */

  @Schema(description = "Patient last name (partial match, case-insensitive).",      example = "Doe", required = false)
  private String lastName;
  /** Medical record number. */

  @Schema(description = "Medical Record Number (exact match). Format:      MRN-YYYY-NNNNNN", example = "MRN-2024-001234", required = false)
  private String medicalRecordNumber;
  /** Date of birth. */

  @Schema(description = "Exact date of birth (ISO 8601). Use in place of a range      when exact DOB is known.", example = "1985-03-15", required = false,      pattern = "^\\d{4}-\\d{2}-\\d{2}$")
  private LocalDate dateOfBirth;
  /** Date of birth from. */

  @Schema(description = "Start of date of birth range (inclusive).", example =      "1970-01-01", required = false, pattern = "^\\d{4}-\\d{2}-\\d{2}$")
  private LocalDate dateOfBirthFrom;
  /** Date of birth to. */

  @Schema(description = "End of date of birth range (inclusive).", example =      "2000-12-31", required = false, pattern = "^\\d{4}-\\d{2}-\\d{2}$")
  private LocalDate dateOfBirthTo;
  /** Gender. */

  @Schema(description = "Gender filter (exact match from enum).", example =      "MALE", required = false, allowableValues = {"MALE", "FEMALE", "OTHER",      "UNKNOWN"})
  private Gender gender;
  /** Email. */

  @Schema(description = "Contact email (partial or exact match).", example =      "john.doe@example.com", required = false)
  private String email;
  /** Phone number. */

  @Schema(description = "Contact phone number (E.164).", example =      "+14155552671", required = false)
  private String phoneNumber;
  /** City. */

  @Schema(description = "City for address filtering (partial match).", example =      "San Francisco", required = false)
  private String city;
  /** State province. */

  @Schema(description = "State or province for address filtering (partial      match).", example = "CA", required = false)
  private String stateProvince;
  /** Postal code. */

  @Schema(description = "Postal code for address filtering.", example = "94105",      required = false)
  private String postalCode;
  /** Blood type. */

  @Schema(description = "Blood type filter (exact match).", example = "O+",      required = false)
  private String bloodType;
  /** Insurance provider. */

  @Schema(description = "Insurance provider name (partial match).", example =      "Acme", required = false)
  private String insuranceProvider;
  /** Status. */

  @Schema(description = "Patient status filter (e.g., ACTIVE, INACTIVE).",      example = "ACTIVE", required = false)
  private Patient.PatientStatus status;
  /** Preferred language. */

  @Schema(description = "Preferred language (ISO code).", example = "EN",      required = false)
  private String preferredLanguage;
  /** Age from. */

  @Schema(description = "Minimum age in years (inclusive).", example = "18",      required = false, minimum = "0")
  private Integer ageFrom;
  /** Age to. */

  @Schema(description = "Maximum age in years (inclusive).", example = "65",      required = false, minimum = "0")
  private Integer ageTo;
  /** Has allergies. */

  @Schema(description = "Filter patients who have recorded allergies.", example      = "true", required = false)
  private Boolean hasAllergies;
  /** Has medical conditions. */

  @Schema(description = "Filter patients who have recorded medical conditions.",      example = "true", required = false)
  private Boolean hasMedicalConditions;
  /** Medical condition. */

  @Schema(description = "Medical condition to filter by (partial match).",      example = "diabetes", required = false)
  private String medicalCondition;
  /** Created date from. */

  @Schema(description = "Start of createdDate range for records (ISO 8601      date).", example = "2024-01-01", required = false, pattern =      "^\\d{4}-\\d{2}-\\d{2}$")
  private LocalDate createdDateFrom;
  /** Created date to. */

  @Schema(description = "End of createdDate range for records (ISO 8601 date).",      example = "2024-12-31", required = false, pattern =      "^\\d{4}-\\d{2}-\\d{2}$")
  private LocalDate createdDateTo;
  /** Created by. */

  @Schema(description = "Filter by creator username (exact or partial match).",      example = "admin", required = false)
  private String createdBy;

  // No-arg constructor for fallback builders and frameworks
  public PatientSearchCriteria() { }

  // Explicit getters for search criteria
  public String getFirstName() { return this.firstName; }
  public String getLastName() { return this.lastName; }
  public String getMedicalRecordNumber() { return this.medicalRecordNumber; }
  public java.time.LocalDate getDateOfBirth() { return this.dateOfBirth; }
  public java.time.LocalDate getDateOfBirthFrom() { return this.dateOfBirthFrom; }
  public java.time.LocalDate getDateOfBirthTo() { return this.dateOfBirthTo; }
  public com.hospital.common.enums.Gender getGender() { return this.gender; }
  public String getEmail() { return this.email; }
  public String getPhoneNumber() { return this.phoneNumber; }
  public String getCity() { return this.city; }
  public String getStateProvince() { return this.stateProvince; }
  public String getPostalCode() { return this.postalCode; }
  public String getBloodType() { return this.bloodType; }
  public String getInsuranceProvider() { return this.insuranceProvider; }
  public com.hospital.patient.domain.Patient.PatientStatus getStatus() { return this.status; }
  public String getPreferredLanguage() { return this.preferredLanguage; }
  public Integer getAgeFrom() { return this.ageFrom; }
  public Integer getAgeTo() { return this.ageTo; }
  public Boolean getHasAllergies() { return this.hasAllergies; }
  public Boolean getHasMedicalConditions() { return this.hasMedicalConditions; }
  public String getMedicalCondition() { return this.medicalCondition; }
  public java.time.LocalDate getCreatedDateFrom() { return this.createdDateFrom; }
  public java.time.LocalDate getCreatedDateTo() { return this.createdDateTo; }
  public String getCreatedBy() { return this.createdBy; }

  // Fallback builder for IDEs without Lombok
  public static PatientSearchCriteriaBuilder builderFallbackInternal() { return new PatientSearchCriteriaBuilder(); }
  // Delegate matching Lombok API
  public static PatientSearchCriteriaBuilder builder() { return builderFallbackInternal(); }

  public static class PatientSearchCriteriaBuilder {
    private String firstName;
    private String lastName;
    private String medicalRecordNumber;

    public PatientSearchCriteriaBuilder firstName(String fn) { this.firstName = fn; return this; }
    public PatientSearchCriteriaBuilder lastName(String ln) { this.lastName = ln; return this; }
    public PatientSearchCriteriaBuilder medicalRecordNumber(String mrn) { this.medicalRecordNumber = mrn; return this; }

    public PatientSearchCriteria build() {
      PatientSearchCriteria c = new PatientSearchCriteria();
      c.firstName = this.firstName;
      c.lastName = this.lastName;
      c.medicalRecordNumber = this.medicalRecordNumber;
      return c;
    }
  }
}
