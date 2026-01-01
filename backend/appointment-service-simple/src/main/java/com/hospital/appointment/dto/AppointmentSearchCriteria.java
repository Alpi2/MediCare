package com.hospital.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.hospital.common.enums.AppointmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Advanced search criteria for filtering appointments. All fields are optional and combined with AND logic.
 */
@Schema(description = "Advanced search criteria for filtering appointments. All fields are optional and combined with AND logic.")
public class AppointmentSearchCriteria {

    // Explicit constructors for compatibility with code and tests
    public AppointmentSearchCriteria() {}

    public AppointmentSearchCriteria(Long patientId, Long doctorId, Long departmentId, List<AppointmentStatus> status, java.time.LocalDate dateFrom, java.time.LocalDate dateTo, java.time.LocalTime startTime, java.time.LocalTime endTime, String appointmentType) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.departmentId = departmentId;
        this.status = status;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentType = appointmentType;
    }

    @Schema(description = "Filter by patient identifier (exact match).", example = "2001")
    private Long patientId;

    @Schema(description = "Filter by doctor identifier (exact match).", example = "3001")
    private Long doctorId;

    @Schema(description = "Filter by department identifier (exact match).", example = "10")
    private Long departmentId;

    @Schema(description = "Filter by one or more workflow statuses. When provided, matches any of the listed statuses.",
            example = "[\"SCHEDULED\", \"CONFIRMED\"]")
    private List<AppointmentStatus> status;

    @Schema(description = "Start of appointment date range (inclusive). Use ISO-8601 date (YYYY-MM-DD).",
            example = "2025-11-01")
    private LocalDate dateFrom;

    @Schema(description = "End of appointment date range (inclusive). Use ISO-8601 date (YYYY-MM-DD).",
            example = "2025-11-30")
    private LocalDate dateTo;

    @Schema(description = "Start time filter for appointments on a date (optional).", example = "08:00")
    private LocalTime startTime;

    @Schema(description = "End time filter for appointments on a date (optional).", example = "17:00")
    private LocalTime endTime;

    @Schema(description = "Filter by appointment type (for example: 'consult', 'follow-up', 'telemedicine').",
            example = "consult")
    private String appointmentType;

    // Backwards-compatible accessor methods for older code that used startDate/endDate
    public LocalDate getStartDate() {
        return this.dateFrom;
    }

    public LocalDate getEndDate() {
        return this.dateTo;
    }

        // Explicit getters for fields used in service logic (avoid Lombok dependency)
        public LocalTime getStartTime() { return this.startTime; }
        public LocalTime getEndTime() { return this.endTime; }
        public Long getPatientId() { return this.patientId; }
        public Long getDoctorId() { return this.doctorId; }
        public Long getDepartmentId() { return this.departmentId; }
        public List<com.hospital.common.enums.AppointmentStatus> getStatus() { return this.status; }

        // Backwards-compatible accessor for appointmentType
        public String getAppointmentType() { return this.appointmentType; }

}
