package com.hospital.appointment.service;

import com.hospital.appointment.dto.*;
import com.hospital.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Akıllı Randevu Servisi Interface
 * 
 * AI Destekli Özellikler:
 * - No-show prediction ile risk skorlama
 * - Reinforcement Learning ile slot optimizasyonu
 * - NLP ile hasta talep analizi
 * - Dynamic priority calculation
 * - Real-time capacity management
 * - Personalized scheduling
 */
public interface AppointmentService {

    /**
     * Temel CRUD Operasyonları
     */
    AppointmentResponse createAppointment(AppointmentCreateRequest request);
    
    AppointmentResponse getAppointmentById(UUID appointmentId);
    
    AppointmentResponse updateAppointment(UUID appointmentId, AppointmentUpdateRequest request);
    
    void deleteAppointment(UUID appointmentId);
    
    Page<AppointmentResponse> searchAppointments(AppointmentSearchCriteria criteria, Pageable pageable);

    /**
     * Hasta ve Doktor Randevuları
     */
    List<AppointmentResponse> getPatientAppointments(UUID patientId, boolean includeHistory);
    
    List<AppointmentResponse> getDoctorAppointments(UUID doctorId, LocalDate date);
    
    List<AppointmentResponse> getDepartmentAppointments(UUID departmentId, LocalDate date);

    /**
     * Randevu Durum Yönetimi
     */
    AppointmentResponse cancelAppointment(UUID appointmentId, AppointmentCancelRequest request);
    
    AppointmentResponse rescheduleAppointment(UUID appointmentId, AppointmentRescheduleRequest request);
    
    AppointmentResponse confirmAppointment(UUID appointmentId);
    
    AppointmentResponse checkInAppointment(UUID appointmentId);
    
    AppointmentResponse startAppointment(UUID appointmentId);
    
    AppointmentResponse completeAppointment(UUID appointmentId, AppointmentCompleteRequest request);
    
    AppointmentResponse markAsNoShow(UUID appointmentId);

    /**
     * AI Destekli Özellikler
     */
    List<AvailableSlotResponse> getAvailableSlots(UUID doctorId, LocalDate date, UUID patientId, int durationMinutes);
    
    List<AppointmentRecommendationResponse> getRecommendedSlots(AppointmentRecommendationRequest request);
    
    NoShowPredictionResponse predictNoShow(UUID appointmentId);
    
    AppointmentOptimizationResponse optimizeSchedule(UUID doctorId, LocalDate date);

    /**
     * İstatistik ve Raporlama
     */
    AppointmentStatisticsResponse getAppointmentStatistics(UUID doctorId, UUID departmentId, 
                                                          LocalDate startDate, LocalDate endDate);
    
    List<AppointmentHistoryResponse> getAppointmentHistory(UUID appointmentId);
    
    AppointmentCapacityResponse getCapacityAnalysis(UUID doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * Bildirim ve Hatırlatma
     */
    void sendAppointmentReminders();
    
    void sendAppointmentConfirmation(UUID appointmentId);
    
    void sendCancellationNotification(UUID appointmentId);
    
    void sendRescheduleNotification(UUID appointmentId);

    /**
     * Toplu İşlemler
     */
    BulkOperationResponse performBulkOperation(BulkAppointmentRequest request);
    
    List<AppointmentResponse> createRecurringAppointments(RecurringAppointmentRequest request);

    /**
     * Entegrasyon ve Senkronizasyon
     */
    void syncWithExternalCalendar(UUID doctorId);
    
    void exportAppointments(AppointmentExportRequest request);
    
    void importAppointments(AppointmentImportRequest request);

    /**
     * Gerçek Zamanlı Özellikler
     */
    void notifyRealTimeUpdates(UUID appointmentId, String eventType);
    
    List<AppointmentResponse> getTodaysAppointments(UUID doctorId);
    
    List<AppointmentResponse> getUpcomingAppointments(UUID patientId, int days);
    
    /**
     * Analitik ve ML
     */
    void updateNoShowPredictionModel();
    
    void trainOptimizationModel();
    
    AppointmentInsightsResponse getAppointmentInsights(UUID doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * Validation ve Business Rules
     */
    boolean isSlotAvailable(UUID doctorId, LocalDate date, int startHour, int startMinute, int durationMinutes);
    
    boolean canPatientBookAppointment(UUID patientId, UUID doctorId);
    
    boolean canAppointmentBeCancelled(UUID appointmentId);
    
    boolean canAppointmentBeRescheduled(UUID appointmentId);

    /**
     * Cache Management
     */
    void refreshAppointmentCache(UUID appointmentId);
    
    void clearDoctorScheduleCache(UUID doctorId, LocalDate date);
    
    void warmupCache();
}