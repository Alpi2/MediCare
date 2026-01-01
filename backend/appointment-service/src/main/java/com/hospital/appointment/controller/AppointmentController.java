package com.hospital.appointment.controller;

import com.hospital.appointment.dto.*;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Akıllı Randevu Sistemi REST Controller
 * 
 * Temel CRUD operasyonları:
 * - Randevu oluşturma (AI destekli optimizasyon)
 * - Randevu listeleme (filtreleme ve sayfalama)
 * - Randevu detay görüntüleme
 * - Randevu güncelleme
 * - Randevu iptal etme
 * - Randevu yeniden planlama
 * 
 * AI Destekli Özellikler:
 * - No-show prediction
 * - Optimal time slot recommendation
 * - Dynamic priority calculation
 * - Real-time capacity management
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointment Management", description = "Akıllı Randevu Yönetim Sistemi API")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Yeni randevu oluşturma - AI destekli optimizasyon
     */
    @PostMapping
    @Operation(summary = "Yeni randevu oluştur", 
               description = "AI destekli optimizasyon ile yeni randevu oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Randevu başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz randevu bilgileri"),
        @ApiResponse(responseCode = "409", description = "Randevu çakışması")
    })
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        
        log.info("Creating new appointment for patient: {}, doctor: {}", 
                request.getPatientId(), request.getDoctorId());
        
        AppointmentResponse response = appointmentService.createAppointment(request);
        
        log.info("Appointment created successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Randevu listesi - filtreleme ve sayfalama
     */
    @GetMapping
    @Operation(summary = "Randevu listesi", 
               description = "Filtreleme ve sayfalama ile randevu listesi")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @Parameter(description = "Hasta ID") @RequestParam(required = false) UUID patientId,
            @Parameter(description = "Doktor ID") @RequestParam(required = false) UUID doctorId,
            @Parameter(description = "Departman ID") @RequestParam(required = false) UUID departmentId,
            @Parameter(description = "Randevu durumu") @RequestParam(required = false) String status,
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        AppointmentSearchCriteria criteria = AppointmentSearchCriteria.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .departmentId(departmentId)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        Page<AppointmentResponse> appointments = appointmentService.searchAppointments(criteria, pageable);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Hasta randevuları
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hasta randevuları", 
               description = "Belirli bir hastanın tüm randevularını getirir")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN') and (#patientId == authentication.principal.patientId or hasRole('ADMIN'))")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "false") boolean includeHistory) {
        
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId, includeHistory);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Doktor randevuları
     */
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Doktor randevuları", 
               description = "Belirli bir doktorun randevularını getirir")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN') and (#doctorId == authentication.principal.doctorId or hasRole('ADMIN'))")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId, date);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Randevu detayı
     */
    @GetMapping("/{appointmentId}")
    @Operation(summary = "Randevu detayı", 
               description = "Belirli bir randevunun detaylarını getirir")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable UUID appointmentId) {
        
        AppointmentResponse appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Randevu güncelleme
     */
    @PutMapping("/{appointmentId}")
    @Operation(summary = "Randevu güncelle", 
               description = "Mevcut randevuyu günceller")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentUpdateRequest request) {
        
        log.info("Updating appointment: {}", appointmentId);
        
        AppointmentResponse response = appointmentService.updateAppointment(appointmentId, request);
        
        log.info("Appointment updated successfully: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Randevu iptal etme
     */
    @PostMapping("/{appointmentId}/cancel")
    @Operation(summary = "Randevu iptal et", 
               description = "Randevuyu iptal eder ve bildirim gönderir")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentCancelRequest request) {
        
        log.info("Cancelling appointment: {} with reason: {}", appointmentId, request.getReason());
        
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, request);
        
        log.info("Appointment cancelled successfully: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Randevu yeniden planlama
     */
    @PostMapping("/{appointmentId}/reschedule")
    @Operation(summary = "Randevu yeniden planla", 
               description = "Randevuyu yeni tarih/saate taşır")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentRescheduleRequest request) {
        
        log.info("Rescheduling appointment: {} to new time: {}", 
                appointmentId, request.getNewDateTime());
        
        AppointmentResponse response = appointmentService.rescheduleAppointment(appointmentId, request);
        
        log.info("Appointment rescheduled successfully: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Randevu onaylama
     */
    @PostMapping("/{appointmentId}/confirm")
    @Operation(summary = "Randevu onayla", 
               description = "Randevuyu onaylar")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable UUID appointmentId) {
        
        log.info("Confirming appointment: {}", appointmentId);
        
        AppointmentResponse response = appointmentService.confirmAppointment(appointmentId);
        
        log.info("Appointment confirmed successfully: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check-in işlemi
     */
    @PostMapping("/{appointmentId}/checkin")
    @Operation(summary = "Check-in", 
               description = "Hasta randevuya check-in yapar")
    @PreAuthorize("hasAnyRole('PATIENT', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> checkInAppointment(@PathVariable UUID appointmentId) {
        
        log.info("Check-in for appointment: {}", appointmentId);
        
        AppointmentResponse response = appointmentService.checkInAppointment(appointmentId);
        
        log.info("Check-in completed for appointment: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Randevu başlatma
     */
    @PostMapping("/{appointmentId}/start")
    @Operation(summary = "Randevu başlat", 
               description = "Doktor randevuyu başlatır")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> startAppointment(@PathVariable UUID appointmentId) {
        
        log.info("Starting appointment: {}", appointmentId);
        
        AppointmentResponse response = appointmentService.startAppointment(appointmentId);
        
        log.info("Appointment started: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Randevu tamamlama
     */
    @PostMapping("/{appointmentId}/complete")
    @Operation(summary = "Randevu tamamla", 
               description = "Randevuyu tamamlar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable UUID appointmentId,
            @RequestBody(required = false) AppointmentCompleteRequest request) {
        
        log.info("Completing appointment: {}", appointmentId);
        
        AppointmentResponse response = appointmentService.completeAppointment(appointmentId, request);
        
        log.info("Appointment completed: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * No-show işaretleme
     */
    @PostMapping("/{appointmentId}/no-show")
    @Operation(summary = "No-show işaretle", 
               description = "Randevuyu no-show olarak işaretler")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> markAsNoShow(@PathVariable UUID appointmentId) {
        
        log.info("Marking appointment as no-show: {}", appointmentId);
        
        AppointmentResponse response = appointmentService.markAsNoShow(appointmentId);
        
        log.info("Appointment marked as no-show: {}", appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Müsait slot'ları getirme - AI destekli
     */
    @GetMapping("/available-slots")
    @Operation(summary = "Müsait slot'lar", 
               description = "AI destekli müsait randevu slot'larını getirir")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @RequestParam UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(defaultValue = "30") int durationMinutes) {
        
        List<AvailableSlotResponse> slots = appointmentService.getAvailableSlots(
                doctorId, date, patientId, durationMinutes);
        
        return ResponseEntity.ok(slots);
    }

    /**
     * Randevu istatistikleri
     */
    @GetMapping("/statistics")
    @Operation(summary = "Randevu istatistikleri", 
               description = "Randevu istatistiklerini getirir")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentStatisticsResponse> getAppointmentStatistics(
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        AppointmentStatisticsResponse statistics = appointmentService.getAppointmentStatistics(
                doctorId, departmentId, startDate, endDate);
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Randevu geçmişi
     */
    @GetMapping("/{appointmentId}/history")
    @Operation(summary = "Randevu geçmişi", 
               description = "Randevunun değişiklik geçmişini getirir")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<AppointmentHistoryResponse>> getAppointmentHistory(
            @PathVariable UUID appointmentId) {
        
        List<AppointmentHistoryResponse> history = appointmentService.getAppointmentHistory(appointmentId);
        return ResponseEntity.ok(history);
    }

    /**
     * Toplu randevu işlemleri
     */
    @PostMapping("/bulk-operations")
    @Operation(summary = "Toplu randevu işlemleri", 
               description = "Birden fazla randevu üzerinde toplu işlem yapar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkOperationResponse> bulkOperations(
            @Valid @RequestBody BulkAppointmentRequest request) {
        
        log.info("Performing bulk operation: {} on {} appointments", 
                request.getOperation(), request.getAppointmentIds().size());
        
        BulkOperationResponse response = appointmentService.performBulkOperation(request);
        
        log.info("Bulk operation completed. Success: {}, Failed: {}", 
                response.getSuccessCount(), response.getFailedCount());
        
        return ResponseEntity.ok(response);
    }
}