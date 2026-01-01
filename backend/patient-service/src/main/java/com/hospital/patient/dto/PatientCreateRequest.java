package com.hospital.patient.dto;


import java.time.LocalDate;

import com.hospital.common.dto.AddressDto;
import com.hospital.common.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
* Patient Create Request DTO
*
* Data transfer object for creating new patients.
* Contains validation rules for patient registration.
*/
@Schema(
description = "Request payload for creating a new patient with demographic and    medical information",
example = "{\"firstName\":\"John\",\"middleName\":\"A\",\"lastName\":\"Doe\"    ,\"dateOfBirth\":\"1985-03-15\",\"gender\":\"MALE\"    ,\"email\":\"john.doe@example.com\",\"phoneNumber\":\"+14155552671\"    ,\"address\":{\"line1\":\"123 Main St\",\"city\":\"Anytown\"    ,\"state\":\"CA\",\"postalCode\":\"94105\",\"country\":\"USA\"}    ,\"bloodType\":\"O+\",\"allergies\":\"Peanuts\"    ,\"medicalConditions\":\"Hypertension\"    ,\"currentMedications\":\"Lisinopril\",\"insuranceProvider\":\"Acme Health\"    ,\"insurancePolicyNumber\":\"POL123456\",\"ssn\":\"123-45-6789\"    ,\"preferredLanguage\":\"EN\",\"notes\":\"NKA\"}"
)
@Data
@Builder
public class PatientCreateRequest {

  @NotBlank(message = "First name is required")
  @Size(min = 1, max = 100, message = "First name must be between 1 and 100      characters")
  @Schema(description = "Patient's legal first name as it appears on      identification documents. Validation: @NotBlank, @Size(1,100)",
  example = "John", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 100)
  /** First name. */
  private String firstName;
  /** Middle name. */

  @Size(max = 100, message = "Middle name cannot exceed 100 characters")
  @Schema(description = "Patient's middle name or initial", example = "A",      requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
  private String middleName;
  /** Last name. */

  @NotBlank(message = "Last name is required")
  @Size(min = 1, max = 100, message = "Last name must be between 1 and 100      characters")
  @Schema(description = "Patient's legal family name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 100)
  private String lastName;

  @NotNull(message = "Date of birth is required")
  @Past(message = "Date of birth must be in the past")
  @Schema(description = "Date of birth in ISO 8601 format (YYYY-MM-DD), must be      in the past. Validation: @NotNull, @Past",
  example = "1985-03-15", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "^\\d{4}-\\d{2}-\\d{2}$")
  /** Date of birth. */
  private LocalDate dateOfBirth;
  /** Gender. */

  @NotNull(message = "Gender is required")
  @Schema(description = "Patient's gender", example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED,      allowableValues = {"MALE", "FEMALE", "OTHER", "UNKNOWN"})
  private Gender gender;
  /** Email. */

  @Email(message = "Invalid email format")
  @Size(max = 255, message = "Email cannot exceed 255 characters")
  @Schema(description = "Contact email address. Validation: @Email,      @Size(max=255)", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED,      maxLength = 255)
  private String email;
  /** Phone number. */

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number      format")
  @Schema(description = "Contact phone number in E.164 format", example =      "+14155552671", requiredMode = Schema.RequiredMode.NOT_REQUIRED, pattern = "^\\+?[1-9]\\d{1,14}$")
  private String phoneNumber;
  /** Emergency contact name. */

  @Size(max = 200, message = "Emergency contact name cannot exceed 200      characters")
  @Schema(description = "Emergency contact person name", example = "Jane Doe",      requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 200)
  private String emergencyContactName;
  /** Emergency contact phone. */

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid emergency contact      phone format")
  @Schema(description = "Emergency contact phone in E.164 format", example =      "+14155551234", requiredMode = Schema.RequiredMode.NOT_REQUIRED, pattern = "^\\+?[1-9]\\d{1,14}$")
  private String emergencyContactPhone;
  /** Emergency contact relationship. */

  @Size(max = 50, message = "Emergency contact relationship cannot exceed 50      characters")
  @Schema(description = "Relationship of emergency contact to patient", example      = "Spouse", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 50)
  private String emergencyContactRelationship;
  /** Address. */

  @Valid
  @Schema(description = "Residential address. See `AddressDto` in      hospital-common for field definitions and constraints.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private AddressDto address;
  /** Blood type. */

  @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type format")
  @Schema(description = "Blood type (A, B, AB, O with + or -)", example = "O+",      requiredMode = Schema.RequiredMode.NOT_REQUIRED, pattern = "^(A|B|AB|O)[+-]$")
  private String bloodType;
  /** Allergies. */

  @Schema(description = "Known allergies (free text)", example = "Peanuts",      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String allergies;
  /** Medical conditions. */

  @Schema(description = "Known medical conditions (free text)", example =      "Hypertension, Type 2 Diabetes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String medicalConditions;
  /** Current medications. */

  @Schema(description = "Current medications (free text)", example = "Lisinopril      10mg once daily", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String currentMedications;
  /** Insurance provider. */

  @Size(max = 200, message = "Insurance provider cannot exceed 200 characters")
  @Schema(description = "Primary insurance provider name", example = "Acme      Health", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 200)
  private String insuranceProvider;
  /** Insurance policy number. */

  @Size(max = 100, message = "Insurance policy number cannot exceed 100      characters")
  @Schema(description = "Insurance policy number", example = "POL123456",      requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
  private String insurancePolicyNumber;
  /** Insurance group number. */

  @Size(max = 100, message = "Insurance group number cannot exceed 100      characters")
  @Schema(description = "Insurance group number", example = "GRP98765", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
  private String insuranceGroupNumber;

  @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "Invalid SSN format")
  @Schema(description = "Social Security Number (must be unique, encrypted at      rest). Validation: @Pattern(^\\d{3}-\\d{2}-\\d{4}$)",
  example = "123-45-6789", requiredMode = Schema.RequiredMode.NOT_REQUIRED, pattern = "^\\d{3}-\\d{2}-\\d{4}$")
  /** Ssn. */
  private String ssn;
  /** Preferred language. */

  @Size(max = 50, message = "Preferred language cannot exceed 50 characters")
  @Schema(description = "Preferred communication language (ISO language code      recommended)", example = "EN", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 50)
  private String preferredLanguage;
  /** Notes. */

  @Schema(description = "Free-text clinical or administrative notes", example =      "No known allergies", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String notes;

  // Explicit public no-arg constructor (used by manual builder fallback and tools)
  public PatientCreateRequest() { }

  // Explicit getters used when Lombok processors are unavailable
  public String getFirstName() { return this.firstName; }
  public String getMiddleName() { return this.middleName; }
  public String getLastName() { return this.lastName; }
  public java.time.LocalDate getDateOfBirth() { return this.dateOfBirth; }
  public com.hospital.common.enums.Gender getGender() { return this.gender; }
  public String getEmail() { return this.email; }
  public String getPhoneNumber() { return this.phoneNumber; }
  public String getEmergencyContactName() { return this.emergencyContactName; }
  public String getEmergencyContactPhone() { return this.emergencyContactPhone; }
  public AddressDto getAddress() { return this.address; }
  public String getBloodType() { return this.bloodType; }
  public String getAllergies() { return this.allergies; }
  public String getMedicalConditions() { return this.medicalConditions; }
  public String getCurrentMedications() { return this.currentMedications; }
  public String getInsuranceProvider() { return this.insuranceProvider; }
  public String getInsurancePolicyNumber() { return this.insurancePolicyNumber; }
  public String getInsuranceGroupNumber() { return this.insuranceGroupNumber; }
  public String getSsn() { return this.ssn; }
  public String getPreferredLanguage() { return this.preferredLanguage; }
  public String getNotes() { return this.notes; }

  // Manual builder fallback for IDEs/language servers that cannot run Lombok      processors
  /**
   * Builder fallback (internal name to avoid builder detection conflicts).
   * @return Result.
   */
  public static PatientCreateRequestBuilder builderFallbackInternal() {
    return new PatientCreateRequestBuilder();
  }

  // Delegate matching Lombok API
  public static PatientCreateRequestBuilder builder() { return builderFallbackInternal(); }

  public static class PatientCreateRequestBuilder {
    /** First name. */
    private String firstName;
    /** Middle name. */
    private String middleName;
    /** Last name. */
    private String lastName;
    /** Date of birth. */
    private java.time.LocalDate dateOfBirth;
    /** Gender. */
    private com.hospital.common.enums.Gender gender;
    /** Email. */
    private String email;
    /** Phone number. */
    private String phoneNumber;
    /** Notes. */
    private String notes;
    /**
     * First name.
     * @param firstName First name.
     * @return Result.
     */

    public PatientCreateRequestBuilder firstName(final String firstName) {        this.firstName = firstName; return this; }
    /**
     * Middle name.
     * @param middleName Middle name.
     * @return Result.
     */
    public PatientCreateRequestBuilder middleName(final String middleName) {        this.middleName = middleName; return this; }
    /**
     * Last name.
     * @param lastName Last name.
     * @return Result.
     */
    public PatientCreateRequestBuilder lastName(final String lastName) {        this.lastName = lastName; return this; }
    /**
     * Date of birth.
     * @param dob Dob.
     * @return Result.
     */
    public PatientCreateRequestBuilder dateOfBirth(java.time.LocalDate dob) {        this.dateOfBirth = dob; return this; }
    /**
     * Gender.
     * @param gender Gender.
     * @return Result.
     */
    public PatientCreateRequestBuilder gender(com.hospital.common.enums.Gender        gender) { this.gender = gender; return this; }
    /**
     * Gender.
     * @param gender Gender.
     * @return Result.
     */
    public PatientCreateRequestBuilder gender(final String gender) { this.gender        = gender != null ? com.hospital.common.enums.Gender.valueOf(gender) :        null; return this; }
    /**
     * Email.
     * @param email Email.
     * @return Result.
     */
    public PatientCreateRequestBuilder email(final String email) { this.email =        email; return this; }
    /**
     * Phone number.
     * @param phoneNumber Phone number.
     * @return Result.
     */
    public PatientCreateRequestBuilder phoneNumber(final String phoneNumber) {        this.phoneNumber = phoneNumber; return this; }
    /**
     * Notes.
     * @param notes Notes.
     * @return Result.
     */
    public PatientCreateRequestBuilder notes(final String notes) { this.notes =        notes; return this; }
    /**
     * Build.
     * @return Result.
     */

    public final PatientCreateRequest build() {
      PatientCreateRequest r = new PatientCreateRequest();
      r.firstName = this.firstName;
      r.middleName = this.middleName;
      r.lastName = this.lastName;
      r.dateOfBirth = this.dateOfBirth;
      r.gender = this.gender;
      r.email = this.email;
      r.phoneNumber = this.phoneNumber;
      r.notes = this.notes;
      return r;
    }
  }
}
