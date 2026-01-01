package com.hospital.appointment.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.AppointmentSearchCriteria;
import com.hospital.appointment.dto.AppointmentUpdateRequest;
import com.hospital.appointment.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Backward compatibility controller that exposes the older base path (/api/appointments)
 * and delegates to the new service handlers. Keep lightweight to avoid duplicating business logic.
 */
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000")
@Validated
@Tag(name = "Appointment Management (compat)", description = "Compatibility endpoints for older clients")
@Deprecated
public class AppointmentCompatibilityController {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AppointmentCompatibilityController.class);

    private final AppointmentService appointmentService;

    @Deprecated(forRemoval = true)
    public AppointmentCompatibilityController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Deprecated
    @Operation(summary = "(compat) Get all appointments with pagination")
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Objects.requireNonNull(sortBy, "sortBy must not be null");
        Objects.requireNonNull(sortDir, "sortDir must not be null");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(appointments);
    }

    @Deprecated
    @Operation(summary = "(compat) Get appointment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Deprecated
    @Operation(summary = "(compat) Get all appointments for a patient")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @Deprecated
    @Operation(summary = "(compat) Get all appointments for a doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
    }

    @Deprecated
    @Operation(summary = "(compat) Create new appointment")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse resp = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Deprecated
    @Operation(summary = "(compat) Update appointment details")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentUpdateRequest request) {
        AppointmentResponse resp = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(resp);
    }

    @Deprecated
    @Operation(summary = "(compat) Delete appointment (hard delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @Deprecated
    @Operation(summary = "(compat) Cancel appointment")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id) {
        AppointmentResponse resp = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(resp);
    }

    @Deprecated
    @Operation(summary = "(compat) Confirm appointment")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long id) {
        AppointmentResponse resp = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(resp);
    }

    @Deprecated
    @Operation(summary = "(compat) Check-in patient for appointment")
    @PostMapping("/{id}/check-in")
    public ResponseEntity<AppointmentResponse> checkInAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.checkInAppointment(id));
    }

    @Deprecated
    @Operation(summary = "(compat) Start appointment (doctor begins consultation)")
    @PostMapping("/{id}/start")
    public ResponseEntity<AppointmentResponse> startAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.startAppointment(id));
    }

    @Deprecated
    @Operation(summary = "(compat) Complete appointment")
    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Long id, @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id, notes));
    }

    @Deprecated
    @Operation(summary = "(compat) Mark appointment as no-show")
    @PostMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markNoShow(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.markNoShow(id));
    }

    @Deprecated
    @Operation(summary = "(compat) Reschedule appointment to new date/time")
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(@PathVariable Long id,
                                                                     @RequestParam LocalDateTime newDateTime,
                                                                     @RequestParam(required = false) Integer newDuration) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, newDateTime, newDuration));
    }

    @Deprecated
    @Operation(summary = "(compat) Search appointments by multiple criteria")
    @PostMapping("/search")
    public ResponseEntity<Page<AppointmentResponse>> searchAppointments(@RequestBody AppointmentSearchCriteria criteria,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(appointmentService.searchAppointments(criteria, pageable));
    }

    @Deprecated
    @Operation(summary = "(compat) Get patient's upcoming appointments")
    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointments(patientId));
    }

    @Deprecated
    @Operation(summary = "(compat) Get doctor's schedule for specific date")
    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<AppointmentResponse>> getDoctorSchedule(@PathVariable Long doctorId, @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getDoctorSchedule(doctorId, date));
    }
}
