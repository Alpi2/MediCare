package com.hospital.appointment.controller;

import com.hospital.appointment.dto.*;
import com.hospital.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@CrossOrigin(origins = "http://localhost:3000")
// Use explicit constructor and logger to avoid relying on Lombok annotation processing
@Validated
@Tag(name = "Appointment Management", description = "APIs for managing appointments and scheduling")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

        private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

        /**
         * Explicit constructor to ensure dependency injection is recognized in environments
         * where Lombok annotation processing may not be available.
         */
        public AppointmentController(AppointmentService appointmentService) {
                this.appointmentService = appointmentService;
        }

    @Operation(summary = "Get all appointments with pagination",
            description = "Returns a paginated list of appointments. Supports sorting by appointmentDateTime and other fields.")
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointment by ID",
            description = "Retrieve a single appointment by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        return appointmentService.getAppointmentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all appointments for a patient",
            description = "List all appointments for a given patient ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class)))
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatientId(
            @Parameter(description = "Patient identifier", example = "123", required = true) @PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @Operation(summary = "Get all appointments for a doctor",
            description = "List all appointments for a given doctor ID.")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctorId(
            @Parameter(description = "Doctor identifier", example = "45", required = true) @PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
    }

    @Operation(summary = "Schedule a new appointment",
            description = "Schedule a new appointment with automatic conflict detection (checks for overlapping doctor appointments), patient validation (verifies patient exists and is active via Patient Service), and business rules enforcement: business hours 08:00-18:00, advance booking between 1 hour and 90 days, allowed durations {15,30,45,60,90,120} minutes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment scheduled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "409", description = "Conflict - scheduling conflict or business rule violation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = "{\"message\":\"Scheduling conflict: provider has overlapping appointment at requested time\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody(description = "Appointment creation payload", required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentCreateRequest.class),
                            examples = @ExampleObject(value = "{\"patientId\":123,\"doctorId\":45,\"startTime\":\"2025-02-20T09:00:00-08:00\",\"durationMinutes\":30,\"locationId\":3,\"reason\":\"Routine follow-up\"}")))
            @Valid @org.springframework.web.bind.annotation.RequestBody AppointmentCreateRequest request) {
        log.info("Creating appointment for patient: {} doctor: {}", request.getPatientId(), request.getDoctorId());
        AppointmentResponse resp = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Update appointment details",
            description = "Update mutable appointment details (e.g., reason, notes). Some fields (patientId, mrn) are immutable once created.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id,
            @RequestBody(description = "Appointment update payload", required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentUpdateRequest.class),
                            examples = @ExampleObject(value = "{\"reason\":\"Updated reason\",\"notes\":\"Patient requested earlier time\"}")))
            @Valid @org.springframework.web.bind.annotation.RequestBody AppointmentUpdateRequest request) {
        AppointmentResponse resp = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Delete appointment (hard delete)",
            description = "Permanently delete an appointment. Typically restricted to administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Appointment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel appointment",
            description = "Cancel an appointment. Can only cancel appointments in SCHEDULED or CONFIRMED status. Cancellation records are kept for audit and may trigger notifications to participants.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment cancelled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - invalid state transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = "{\"message\":\"Cannot cancel appointment in COMPLETED status\"}")))
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        AppointmentResponse resp = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Confirm appointment",
            description = "Confirm a SCHEDULED appointment. Transitions SCHEDULED -> CONFIRMED. Confirmation may validate provider availability again.")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        AppointmentResponse resp = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Check-in patient for appointment",
            description = "Mark a patient as checked-in. Typically allowed in CONFIRMED status and will transition CONFIRMED -> CHECKED_IN.")
    @PostMapping("/{id}/check-in")
    public ResponseEntity<AppointmentResponse> checkInAppointment(@Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.checkInAppointment(id));
    }

    @Operation(summary = "Start appointment (doctor begins consultation)",
            description = "Transition appointment to STARTED when the provider begins the consultation. Allowed from CHECKED_IN or CONFIRMED depending on workflow.")
    @PostMapping("/{id}/start")
    public ResponseEntity<AppointmentResponse> startAppointment(@Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.startAppointment(id));
    }

    @Operation(summary = "Complete appointment",
            description = "Mark appointment as COMPLETED. Allowed from STARTED. Completion will finalize encounter details and may trigger billing/workflow tasks.")
    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id,
            @Parameter(description = "Optional clinician notes", example = "Patient responded well to treatment") @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id, notes));
    }

    @Operation(summary = "Mark appointment as no-show",
            description = "Mark an appointment as NO_SHOW. Allowed for SCHEDULED or CONFIRMED appointments if patient did not arrive within grace period.")
    @PostMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markNoShow(@Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.markNoShow(id));
    }

    @Operation(summary = "Reschedule appointment to new date/time",
            description = "Move appointment to new date/time with same validation as creation (conflict detection, patient validation, business hours and duration rules). Preserves appointment history and creates an audit record for the change.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment rescheduled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - scheduling conflict",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = "{\"message\":\"Scheduling conflict: provider has overlapping appointment\"}")))
    })
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @Parameter(description = "Appointment identifier", example = "456", required = true) @PathVariable Long id,
            @Parameter(description = "New appointment start time in ISO 8601 with timezone", example = "2025-02-20T10:00:00-08:00", required = true) @RequestParam LocalDateTime newDateTime,
            @Parameter(description = "New duration in minutes (allowed: 15,30,45,60,90,120)", example = "30", required = false) @RequestParam(required = false) Integer newDuration) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, newDateTime, newDuration));
    }

    @Operation(summary = "Search appointments by multiple criteria",
            description = "Advanced search supporting filters (patientId, doctorId, date ranges, status, location, etc.). All filters are combined with AND logic.")
    @PostMapping("/search")
    public ResponseEntity<Page<AppointmentResponse>> searchAppointments(
            @RequestBody(description = "Search criteria payload", required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentSearchCriteria.class),
                            examples = @ExampleObject(value = "{\"patientId\":123,\"dateFrom\":\"2025-02-01\",\"dateTo\":\"2025-02-28\"}"))) @org.springframework.web.bind.annotation.RequestBody AppointmentSearchCriteria criteria,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(appointmentService.searchAppointments(criteria, pageable));
    }

    @Operation(summary = "Get patient's upcoming appointments",
            description = "Return upcoming appointments for a patient ordered by start time.")
    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments(@Parameter(description = "Patient identifier", example = "123", required = true) @PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointments(patientId));
    }

    @Operation(summary = "Get doctor's schedule for specific date",
            description = "Return the schedule for a doctor on a given date, including time slots and appointment statuses.")
    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<AppointmentResponse>> getDoctorSchedule(
            @Parameter(description = "Doctor identifier", example = "45", required = true) @PathVariable Long doctorId,
            @Parameter(description = "Date to retrieve schedule for (ISO 8601)", example = "2025-02-20", required = true) @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getDoctorSchedule(doctorId, date));
    }
}