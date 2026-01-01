package com.hospital.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request payload for updating appointment details. Only provided fields are updated.
 * Status changes should use dedicated workflow endpoints (confirm, cancel, etc.).
 */
@Schema(description = "Request payload for updating appointment details. Only provided fields are updated. Status changes should use dedicated workflow endpoints (confirm, cancel, etc.).")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentUpdateRequest {

    @Schema(description = "Unique identifier of the appointment. Usually supplied as path variable; provided here for reference.",
            example = "a1b2c3d4-5e6f-7a8b-9c0d-1234567890ab")
    private String appointmentId;

    @Schema(description = "Patient identifier (not updatable). Included for reference only; changes to patient must be performed via administrative workflows.",
            example = "patient-123",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String patientId;

    @Schema(description = "Doctor identifier (not updatable). Included for reference only; re-assignment of a doctor is not permitted via this endpoint.",
            example = "doctor-456",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String doctorId;

    @Future
    @Schema(description = "Appointment date and time in ISO-8601 format. Providing this field triggers server-side conflict detection and may return HTTP 409 Conflict if the requested slot overlaps existing bookings.",
            example = "2025-12-01T14:30:00")
    private LocalDateTime appointmentDateTime;

    @Min(15)
    @Max(240)
    @Schema(description = "Duration of the appointment in minutes. If omitted the existing duration remains unchanged.", example = "30")
    private Integer durationMinutes;

    @Schema(description = "Location or room for the appointment (e.g., 'Room 201' or 'Telemedicine'). If omitted the existing location remains unchanged.",
            example = "Room 201")
    private String location;

    @Schema(description = "Type of appointment (for example: 'consult', 'follow-up', 'telemedicine'). Unknown values will be validated against the service's allowed set.",
            example = "consult")
    private String appointmentType;

    @Size(max = 1000)
    @Schema(description = "Optional free-text notes or instructions for staff. Maximum length is implementation-dependent.",
            example = "Patient prefers afternoon slots; bring prior imaging if available.")
    private String notes;

    @Size(max = 500)
    @Schema(description = "Optional reason for the update, used by audit/logging systems.", example = "Rescheduling due to doctor unavailability.")
    private String reason;

        // Explicit getters/setters to avoid Lombok dependency for IDEs/build setups without annotation processing
        public String getAppointmentId() { return this.appointmentId; }
        public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

        public String getPatientId() { return this.patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }

        public String getDoctorId() { return this.doctorId; }
        public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

        public LocalDateTime getAppointmentDateTime() { return this.appointmentDateTime; }
        public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

        public Integer getDurationMinutes() { return this.durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getNotes() { return this.notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getReason() { return this.reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getStatus() { return this.status; }
        public void setStatus(String status) { this.status = status; }

    @Schema(description = "Appointment status is not updatable via this endpoint — use dedicated workflow endpoints (confirm, cancel, check-in, complete).",
            example = "SCHEDULED",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    // Manual builder fallback for IDEs/language servers that cannot run Lombok processors
    public static AppointmentUpdateRequestBuilder builderFallbackInternal() {
        return new AppointmentUpdateRequestBuilder();
    }
    // Delegate matching Lombok API
    public static AppointmentUpdateRequestBuilder builder() { return builderFallbackInternal(); }

    public static class AppointmentUpdateRequestBuilder {
        private java.time.LocalDateTime appointmentDateTime;
        private Integer durationMinutes;
        private String location;
        private String appointmentType;
        private String notes;
        private String reason;

        public AppointmentUpdateRequestBuilder appointmentDateTime(java.time.LocalDateTime dt) { this.appointmentDateTime = dt; return this; }
        public AppointmentUpdateRequestBuilder durationMinutes(Integer d) { this.durationMinutes = d; return this; }
        public AppointmentUpdateRequestBuilder location(String l) { this.location = l; return this; }
        public AppointmentUpdateRequestBuilder appointmentType(String t) { this.appointmentType = t; return this; }
        public AppointmentUpdateRequestBuilder notes(String n) { this.notes = n; return this; }
        public AppointmentUpdateRequestBuilder reason(String r) { this.reason = r; return this; }

        public AppointmentUpdateRequest build() {
            AppointmentUpdateRequest r = new AppointmentUpdateRequest();
            r.appointmentDateTime = this.appointmentDateTime;
            r.durationMinutes = this.durationMinutes;
            r.location = this.location;
            r.appointmentType = this.appointmentType;
            r.notes = this.notes;
            r.reason = this.reason;
            return r;
        }
    }

    // return a builder populated from this instance (compatibility with toBuilder usage)
    public AppointmentUpdateRequestBuilder toBuilder() {
        AppointmentUpdateRequestBuilder b = new AppointmentUpdateRequestBuilder();
        b.appointmentDateTime = this.appointmentDateTime;
        b.durationMinutes = this.durationMinutes;
        b.location = this.location;
        b.appointmentType = this.appointmentType;
        b.notes = this.notes;
        b.reason = this.reason;
        return b;
    }

}
