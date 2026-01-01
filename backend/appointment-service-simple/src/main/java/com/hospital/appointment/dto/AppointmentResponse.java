package com.hospital.appointment.dto;

import com.hospital.common.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;


import java.time.LocalDateTime;

@Schema(description = "Appointment information with enriched patient data and computed fields.",
    example = """
    [
      {
        "id": 1001,
        "patientId": 2001,
        "patientName": "Jane Doe",
        "doctorId": 3001,
        "doctorName": "Dr. John Smith",
        "departmentId": 10,
        "appointmentDateTime": "2025-12-01T14:30:00",
        "durationMinutes": 30,
        "status": "SCHEDULED",
        "appointmentType": "consult",
        "notes": "Bring prior imaging.",
        "canCancel": true,
        "canReschedule": true,
        "createdAt": "2025-11-01T09:00:00",
        "updatedAt": "2025-11-15T10:00:00"
      },
      {
        "id": 1002,
        "patientId": 2002,
        "patientName": "John Roe",
        "doctorId": 3002,
        "doctorName": "Dr. Alice Chen",
        "departmentId": 12,
        "appointmentDateTime": "2025-11-20T10:00:00",
        "durationMinutes": 20,
        "status": "CONFIRMED",
        "appointmentType": "follow-up",
        "notes": "Patient called to confirm.",
        "canCancel": true,
        "canReschedule": false,
        "createdAt": "2025-10-01T09:00:00",
        "updatedAt": "2025-11-10T08:00:00"
      },
      {
        "id": 1003,
        "patientId": 2003,
        "patientName": "Alice Example",
        "doctorId": 3003,
        "doctorName": "Dr. Bob Lee",
        "departmentId": 14,
        "appointmentDateTime": "2025-10-01T09:00:00",
        "durationMinutes": 45,
        "status": "COMPLETED",
        "appointmentType": "telemedicine",
        "notes": "Completed appointment.",
        "canCancel": false,
        "canReschedule": false,
        "createdAt": "2025-09-01T09:00:00",
        "updatedAt": "2025-10-01T11:00:00"
      }
    ]
    """)

public class AppointmentResponse {

    @Schema(description = "Unique appointment identifier.", example = "1001")
    private Long id;

    @Schema(description = "Identifier of the patient (reference to Patient Service).",
        example = "2001")
    private Long patientId;

    @Schema(description = "Enriched patient name retrieved from Patient Service in the format 'FirstName LastName'.",
        example = "Jane Doe",
        accessMode = Schema.AccessMode.READ_ONLY)
    private String patientName;

    @Schema(description = "Identifier of the assigned doctor.", example = "3001")
    private Long doctorId;

    @Schema(description = "Doctor's display name.", example = "Dr. John Smith",
        accessMode = Schema.AccessMode.READ_ONLY)
    private String doctorName;

    @Schema(description = "Identifier of the department where the appointment will take place.", example = "10")
    private Long departmentId;

    @Schema(description = "Scheduled date and time of the appointment (server local timezone or ISO-8601).",
        example = "2025-12-01T14:30:00")
    private LocalDateTime appointmentDateTime;

    @Schema(description = "Current workflow state of the appointment. See `AppointmentStatus` enum in `hospital-common`.",
        implementation = AppointmentStatus.class,
        example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "Free-text notes attached to the appointment.", example = "Bring prior imaging if available.")
    private String notes;

    @Schema(description = "Optional administrative reason (e.g., for cancellation).",
        example = "Patient requested reschedule due to travel.")
    private String reason;

    @Schema(description = "Duration of the appointment in minutes.", example = "30")
    private Integer durationMinutes;

    @Schema(description = "Creation timestamp of the appointment record.", example = "2025-11-01T09:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp of the appointment record.", example = "2025-11-15T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Computed: whether the appointment may currently be cancelled (depends on status and timing).",
        example = "true",
        accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean canCancel;

    @Schema(description = "Computed: whether the appointment may currently be rescheduled (depends on status and timing).",
        example = "true",
        accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean canReschedule;

    @Schema(description = "Computed flag indicating whether the appointment time is in the past.",
        example = "false",
        accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isPast;

    // Explicit getters to avoid relying on Lombok annotation processing in some IDEs
    public Long getId() {
        return this.id;
    }

    public Long getPatientId() {
        return this.patientId;
    }

    public String getPatientName() {
        return this.patientName;
    }

    public Long getDoctorId() {
        return this.doctorId;
    }

    public String getDoctorName() {
        return this.doctorName;
    }

    public Long getDepartmentId() {
        return this.departmentId;
    }

    public java.time.LocalDateTime getAppointmentDateTime() {
        return this.appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return this.status;
    }

    public String getNotes() {
        return this.notes;
    }

    public String getReason() {
        return this.reason;
    }

    public Integer getDurationMinutes() {
        return this.durationMinutes;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Boolean getCanCancel() {
        return this.canCancel;
    }

    public Boolean getCanReschedule() {
        return this.canReschedule;
    }

    public Boolean getIsPast() {
        return this.isPast;
    }

    // Compatibility boolean-style accessors used by existing tests
    public boolean isPast() { return Boolean.TRUE.equals(this.isPast); }
    public boolean isCanCancel() { return Boolean.TRUE.equals(this.canCancel); }
    public boolean isCanReschedule() { return Boolean.TRUE.equals(this.canReschedule); }

    // Explicit setters used by mappers and manual construction when Lombok isn't processed by the IDE
    public void setId(Long id) {
        this.id = id;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public void setAppointmentDateTime(java.time.LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public void setCanReschedule(Boolean canReschedule) {
        this.canReschedule = canReschedule;
    }

    public void setIsPast(Boolean isPast) {
        this.isPast = isPast;
    }
}
