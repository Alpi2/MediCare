package com.hospital.appointment.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Request payload for scheduling a new appointment. All business rules are validated before creation.",
        example = "{\"patientId\":123,\"doctorId\":789,\"departmentId\":10,\"appointmentDateTime\":\"2025-02-20T14:00:00-08:00\",\"durationMinutes\":30,\"appointmentType\":\"CONSULTATION\",\"reason\":\"Routine follow-up\",\"notes\":\"Patient reports chest pain\"}"
)
public class AppointmentCreateRequest {

    @NotNull
    @Positive
    @Schema(description = "Patient ID (must exist in Patient Service and have ACTIVE status)", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long patientId;

    @Positive
    @Schema(description = "Doctor ID (optional for walk-in appointments)", example = "789", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long doctorId;

    @Positive
    @Schema(description = "Department or unit identifier (optional)", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long departmentId;

    @NotNull
    @Future
    @Schema(description = "Appointment date and time in ISO 8601 format. Must be: in future, within business hours (08:00-18:00), on weekday (Monday-Friday), between 1 hour and 90 days from now",
            example = "2024-12-15T14:00:00-08:00", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "ISO 8601")
    private LocalDateTime appointmentDateTime;

    @NotNull
    @Min(15)
    @Max(240)
    @Schema(description = "Appointment duration in minutes", example = "30", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"15", "30", "45", "60", "90", "120"})
    private Integer durationMinutes;

    @Schema(description = "Type of appointment", example = "CONSULTATION", allowableValues = {"CONSULTATION", "FOLLOW_UP", "EMERGENCY", "ROUTINE_CHECKUP"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String appointmentType;

    @Size(max = 1000)
    @Schema(description = "Additional notes or reason for visit", example = "Patient reports chest pain", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 1000)
    private String notes;

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Primary reason for visit (short)", example = "Routine follow-up", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    private String reason;

        // Explicit getters to avoid relying on Lombok annotation processing in some IDEs/build setups
        public LocalDateTime getAppointmentDateTime() { return this.appointmentDateTime; }
        public Integer getDurationMinutes() { return this.durationMinutes; }
        public String getNotes() { return this.notes; }
        public String getReason() { return this.reason; }

        // Explicit getters to avoid dependency on Lombok annotation processing in some IDEs/build setups
        public Long getPatientId() {
                return this.patientId;
        }

        public Long getDoctorId() {
                return this.doctorId;
        }

        public Long getDepartmentId() {
                return this.departmentId;
        }

        // Explicit setters for manual construction/fallbacks
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        // Manual builder fallback for IDEs/language servers that cannot run Lombok processors
        public static AppointmentCreateRequestBuilder builderFallbackInternal() {
            return new AppointmentCreateRequestBuilder();
        }
        // Delegate matching Lombok API
        public static AppointmentCreateRequestBuilder builder() { return builderFallbackInternal(); }

        public static class AppointmentCreateRequestBuilder {
            private Long patientId;
            private Long doctorId;
            private Long departmentId;
            private java.time.LocalDateTime appointmentDateTime;
            private Integer durationMinutes;
            private String appointmentType;
            private String notes;
            private String reason;

            public AppointmentCreateRequestBuilder patientId(Long id) { this.patientId = id; return this; }
            public AppointmentCreateRequestBuilder doctorId(Long id) { this.doctorId = id; return this; }
            public AppointmentCreateRequestBuilder departmentId(Long id) { this.departmentId = id; return this; }
            public AppointmentCreateRequestBuilder appointmentDateTime(java.time.LocalDateTime dt) { this.appointmentDateTime = dt; return this; }
            public AppointmentCreateRequestBuilder durationMinutes(Integer d) { this.durationMinutes = d; return this; }
            public AppointmentCreateRequestBuilder appointmentType(String t) { this.appointmentType = t; return this; }
            public AppointmentCreateRequestBuilder notes(String n) { this.notes = n; return this; }
            public AppointmentCreateRequestBuilder reason(String r) { this.reason = r; return this; }

            public AppointmentCreateRequest build() {
                AppointmentCreateRequest r = new AppointmentCreateRequest();
                r.patientId = this.patientId;
                r.doctorId = this.doctorId;
                r.departmentId = this.departmentId;
                r.appointmentDateTime = this.appointmentDateTime;
                r.durationMinutes = this.durationMinutes;
                r.appointmentType = this.appointmentType;
                r.notes = this.notes;
                r.reason = this.reason;
                return r;
            }
        }

    // Return a builder pre-populated from this instance (compatibility with code using toBuilder())
    public AppointmentCreateRequestBuilder toBuilder() {
        AppointmentCreateRequestBuilder b = new AppointmentCreateRequestBuilder();
        b.patientId = this.patientId;
        b.doctorId = this.doctorId;
        b.departmentId = this.departmentId;
        b.appointmentDateTime = this.appointmentDateTime;
        b.durationMinutes = this.durationMinutes;
        b.appointmentType = this.appointmentType;
        b.notes = this.notes;
        b.reason = this.reason;
        return b;
    }
}
