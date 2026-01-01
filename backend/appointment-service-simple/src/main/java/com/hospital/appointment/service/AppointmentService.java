package com.hospital.appointment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.client.PatientServiceClient;
import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.AppointmentSearchCriteria;
import com.hospital.appointment.dto.AppointmentUpdateRequest;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.common.enums.AppointmentStatus;
import com.hospital.common.event.AppointmentEvent;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.common.exception.ValidationException;
import com.hospital.common.kafka.KafkaProducerService;

import jakarta.annotation.PostConstruct;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final KafkaProducerService kafkaProducerService;
    private final PatientServiceClient patientServiceClient;
    private final AppointmentMapper appointmentMapper;
    private final io.micrometer.core.instrument.Counter appointmentCreationCounter;
    private final io.micrometer.core.instrument.Counter appointmentCancellationCounter;
    private final io.micrometer.core.instrument.Counter noShowCounter;
    private final io.micrometer.core.instrument.Counter schedulingConflictCounter;

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    /**
     * Explicit constructor replacing Lombok's {@code @RequiredArgsConstructor} so IDEs
     * and environments without Lombok annotation processing still recognize
     * constructor injection for all required dependencies.
     */
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            KafkaProducerService kafkaProducerService,
            PatientServiceClient patientServiceClient,
            AppointmentMapper appointmentMapper,
            io.micrometer.core.instrument.Counter appointmentCreationCounter,
            io.micrometer.core.instrument.Counter appointmentCancellationCounter,
            io.micrometer.core.instrument.Counter noShowCounter,
            io.micrometer.core.instrument.Counter schedulingConflictCounter) {
        this.appointmentRepository = appointmentRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.patientServiceClient = patientServiceClient;
        this.appointmentMapper = appointmentMapper;
        this.appointmentCreationCounter = appointmentCreationCounter;
        this.appointmentCancellationCounter = appointmentCancellationCounter;
        this.noShowCounter = noShowCounter;
        this.schedulingConflictCounter = schedulingConflictCounter;
    }

    @Value("${appointment.advance-booking.min-hours:1}")
    private int minAdvanceHours;

    @Value("${appointment.advance-booking.max-days:90}")
    private int maxDaysInAdvance;

    @Value("${appointment.business-hours.start:08:00}")
    private String businessHoursStartStr;

    @Value("${appointment.business-hours.end:18:00}")
    private String businessHoursEndStr;

    @Value("${appointment.duration.allowed-values:}")
    private String allowedDurationsStr;

    @Value("${appointment.allow-weekend-booking:false}")
    private boolean allowWeekendBooking;

    private int businessHourStart;
    private int businessHourEnd;
    private Set<Integer> allowedDurations;

    @Value("${kafka.topics.appointment-events}")
    private String appointmentEventsTopic;

    @Cacheable(value = "appointments-page", key = "'all-'+#pageable.pageNumber+'-'+#pageable.pageSize")
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        log.debug("Fetching all appointments, page: {} size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Appointment> page = appointmentRepository.findAll(pageable);
        return page.map(appointmentMapper::toResponse);
    }

    @Cacheable(value = "appointment-by-id", key = "#id")
    public Optional<AppointmentResponse> getAppointmentById(Long id) {
        log.debug("Fetching appointment by id: {}", id);
        return appointmentRepository.findById(id).map(appointmentMapper::toResponse);
    }

    @Cacheable(value = "patient-appointments", key = "#patientId")
    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        log.debug("Fetching appointments for patient: {}", patientId);
        List<Appointment> list = appointmentRepository.findByPatientId(patientId);
        return appointmentMapper.toResponseList(list);
    }

    @Cacheable(value = "doctor-schedule", key = "#doctorId")
    public List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId) {
        log.debug("Fetching appointments for doctor: {}", doctorId);
        List<Appointment> list = appointmentRepository.findByDoctorId(doctorId);
        return appointmentMapper.toResponseList(list);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        Long patientId = Objects.requireNonNull(request.getPatientId(), "patientId is required");
        Long doctorId = Objects.requireNonNull(request.getDoctorId(), "doctorId is required");
        log.info("Creating appointment for patient: {} doctor: {}", patientId, doctorId);
        if (!patientServiceClient.validatePatientExists(patientId)) {
            throw new ResourceNotFoundException("Patient", "id", patientId);
        }
        if (!patientServiceClient.isPatientActive(patientId)) {
            throw new ValidationException("Patient is not active");
        }
        validateAppointmentTime(request.getAppointmentDateTime(), request.getDurationMinutes());
        checkDoctorAvailability(doctorId, request.getAppointmentDateTime(), request.getDurationMinutes());

        Appointment entity = appointmentMapper.toEntity(request);
        Appointment saved = appointmentRepository.save(entity);
        Long savedId = Objects.requireNonNull(saved.getId());
        Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
        Long savedDoctorId = Objects.requireNonNull(saved.getDoctorId(), "saved.doctorId is required");

        AppointmentEvent event = AppointmentEvent.created(
                savedId, savedPatientId, savedDoctorId, saved.getAppointmentDateTime(), saved.getDurationMinutes());
        try {
            kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event);
        } catch (Exception e) {
            log.warn("Failed to publish appointment.created for id {}: {}", savedId, e.getMessage());
        }
        log.info("Appointment created successfully: id={}, patient={}, doctor={}", savedId, savedPatientId, savedDoctorId);
        try {
            appointmentCreationCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to increment appointment creation counter: {}", e.getMessage());
        }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request) {
        log.info("Updating appointment: {}", id);
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (request.getAppointmentDateTime() != null) {
            validateAppointmentTime(request.getAppointmentDateTime(), request.getDurationMinutes() != null ? request.getDurationMinutes() : appointment.getDurationMinutes());
            checkDoctorAvailability(appointment.getDoctorId(), request.getAppointmentDateTime(), request.getDurationMinutes() != null ? request.getDurationMinutes() : appointment.getDurationMinutes());
        }
        appointmentMapper.updateEntityFromRequest(request, appointment);
    Appointment saved = appointmentRepository.save(appointment);
    Long savedId = Objects.requireNonNull(saved.getId());
    AppointmentEvent event = AppointmentEvent.updated(savedId, saved.getPatientId(), saved.getDoctorId(), saved.getStatus());
    try { kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event); } catch (Exception e) { log.warn("Failed to publish appointment.updated for id {}: {}", savedId, e.getMessage()); }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public void deleteAppointment(Long id) {
        log.warn("Deleting appointment: {}", id);
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        Long patientId = Objects.requireNonNull(appointment.getPatientId(), "appointment.patientId is required");
    appointmentRepository.deleteById(id);
    try {
        AppointmentEvent event = AppointmentEvent.deleted(id, patientId);
        kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(id), event);
    } catch (Exception e) { log.warn("Failed to publish appointment.deleted for id {}: {}", id, e.getMessage()); }
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (appointment.getStatus().isFinal()) throw new ValidationException("Cannot cancel a final appointment");
        if (appointment.getAppointmentDateTime() != null && appointment.getAppointmentDateTime().isBefore(LocalDateTime.now())) throw new ValidationException("Cannot cancel past appointments");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);
        Long savedId = Objects.requireNonNull(saved.getId());
        Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
        try {
            kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), AppointmentEvent.cancelled(savedId, savedPatientId, saved.getNotes()));
        } catch (Exception e) {
            log.warn("Failed to publish appointment.cancelled for id {}: {}", savedId, e.getMessage());
        }
        try {
            appointmentCancellationCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to increment appointment cancellation counter: {}", e.getMessage());
        }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) throw new ValidationException("Only scheduled appointments can be confirmed");
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointmentRepository.save(appointment);
        Long savedId = Objects.requireNonNull(saved.getId());
        Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
        try { kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), AppointmentEvent.confirmed(savedId, savedPatientId)); } catch (Exception e) { log.warn("Failed to publish appointment.confirmed for id {}: {}", savedId, e.getMessage()); }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse checkInAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (!(appointment.getStatus() == AppointmentStatus.CONFIRMED || appointment.getStatus() == AppointmentStatus.SCHEDULED)) throw new ValidationException("Appointment cannot be checked in from current status");
        LocalDateTime now = LocalDateTime.now();
        if (appointment.getAppointmentDateTime() == null || appointment.getAppointmentDateTime().toLocalDate().isBefore(now.toLocalDate())) throw new ValidationException("Can only check-in on appointment day");
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
    Appointment saved = appointmentRepository.save(appointment);
    Long savedId = Objects.requireNonNull(saved.getId());
    Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
    try {
        AppointmentEvent event = AppointmentEvent.checkedIn(savedId, savedPatientId);
        kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event);
    } catch (Exception e) { log.warn("Failed to publish appointment.checked_in for id {}: {}", savedId, e.getMessage()); }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse startAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) throw new ValidationException("Appointment must be checked in to start");
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
    Appointment saved = appointmentRepository.save(appointment);
    Long savedId = Objects.requireNonNull(saved.getId());
    Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
    try {
        AppointmentEvent event = AppointmentEvent.started(savedId, savedPatientId);
        kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event);
    } catch (Exception e) { log.warn("Failed to publish appointment.started for id {}: {}", savedId, e.getMessage()); }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse completeAppointment(Long id, String notes) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) throw new ValidationException("Appointment must be in progress to complete");
        if (notes != null) appointment.setNotes(appointment.getNotes() == null ? notes : appointment.getNotes() + "\n" + notes);
        appointment.setStatus(AppointmentStatus.COMPLETED);
    Appointment saved = appointmentRepository.save(appointment);
    Long savedId = Objects.requireNonNull(saved.getId());
    Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
    try {
        AppointmentEvent event = AppointmentEvent.completedEvent(savedId, savedPatientId, saved.getNotes());
        kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event);
    } catch (Exception e) { log.warn("Failed to publish appointment.completed for id {}: {}", savedId, e.getMessage()); }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse markNoShow(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (!(appointment.getStatus() == AppointmentStatus.SCHEDULED || appointment.getStatus() == AppointmentStatus.CONFIRMED)) throw new ValidationException("Only scheduled or confirmed appointments can be marked no-show");
        if (appointment.getAppointmentDateTime() == null || appointment.getAppointmentDateTime().isAfter(LocalDateTime.now().minusMinutes(15))) throw new ValidationException("Appointment must be past to mark no-show");
        appointment.setStatus(AppointmentStatus.NO_SHOW);
    Appointment saved = appointmentRepository.save(appointment);
    Long savedId = Objects.requireNonNull(saved.getId());
    Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
    try {
        AppointmentEvent event = AppointmentEvent.noShow(savedId, savedPatientId);
        kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event);
    } catch (Exception e) { log.warn("Failed to publish appointment.no_show for id {}: {}", savedId, e.getMessage()); }
        try {
            noShowCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to increment no-show counter: {}", e.getMessage());
        }
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"appointment-by-id", "appointments-page", "patient-appointments", "doctor-schedule"}, allEntries = true)
    public AppointmentResponse rescheduleAppointment(Long id, LocalDateTime newDateTime, Integer newDuration) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (!(appointment.getStatus() == AppointmentStatus.SCHEDULED || appointment.getStatus() == AppointmentStatus.CONFIRMED)) throw new ValidationException("Only scheduled/confirmed appointments can be rescheduled");
        validateAppointmentTime(newDateTime, newDuration != null ? newDuration : appointment.getDurationMinutes());
        checkDoctorAvailability(appointment.getDoctorId(), newDateTime, newDuration != null ? newDuration : appointment.getDurationMinutes());
        appointment.setAppointmentDateTime(newDateTime);
        if (newDuration != null) appointment.setDurationMinutes(newDuration);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
    Appointment saved = appointmentRepository.save(appointment);
    Long savedId = Objects.requireNonNull(saved.getId());
    Long savedPatientId = Objects.requireNonNull(saved.getPatientId(), "saved.patientId is required");
    try {
        AppointmentEvent event = AppointmentEvent.rescheduled(savedId, savedPatientId, saved.getAppointmentDateTime(), saved.getDurationMinutes());
        kafkaProducerService.sendEvent(appointmentEventsTopic, String.valueOf(savedId), event);
    } catch (Exception e) { log.warn("Failed to publish appointment.rescheduled for id {}: {}", savedId, e.getMessage()); }
        return appointmentMapper.toResponse(saved);
    }

    public Page<AppointmentResponse> searchAppointments(AppointmentSearchCriteria criteria, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        log.debug("Searching appointments with criteria: {}", criteria);

        Specification<Appointment> spec = Specification.where(null);

        if (criteria.getPatientId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("patientId"), criteria.getPatientId()));
        }
        if (criteria.getDoctorId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("doctorId"), criteria.getDoctorId()));
        }
        if (criteria.getDepartmentId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("departmentId"), criteria.getDepartmentId()));
        }
        if (criteria.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.getStatus()));
        }
        if (criteria.getStartDate() != null || criteria.getEndDate() != null) {
            java.time.LocalDateTime start = null;
            java.time.LocalDateTime end = null;
            if (criteria.getStartDate() != null) {
                start = criteria.getStartDate().atTime(criteria.getStartTime() != null ? criteria.getStartTime() : java.time.LocalTime.MIN);
            }
            if (criteria.getEndDate() != null) {
                end = criteria.getEndDate().atTime(criteria.getEndTime() != null ? criteria.getEndTime() : java.time.LocalTime.MAX);
            }
            if (start != null && end != null) {
                final java.time.LocalDateTime s = start;
                final java.time.LocalDateTime e = end;
                spec = spec.and((root, query, cb) -> cb.between(root.get("appointmentDateTime"), s, e));
            } else if (start != null) {
                final java.time.LocalDateTime s = start;
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("appointmentDateTime"), s));
            } else if (end != null) {
                final java.time.LocalDateTime e = end;
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("appointmentDateTime"), e));
            }
        }

        Page<Appointment> page = appointmentRepository.findAll(spec, pageable);
        return page.map(appointmentMapper::toResponse);
    }

    @Cacheable(value = "patient-appointments", key = "'upcoming-'+#patientId")
    public List<AppointmentResponse> getUpcomingAppointments(Long patientId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.List<com.hospital.common.enums.AppointmentStatus> statuses = java.util.Arrays.asList(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        List<Appointment> list = appointmentRepository.findByPatientIdAndAppointmentDateTimeAfterAndStatusIn(patientId, now, statuses, Sort.by(Sort.Direction.ASC, "appointmentDateTime"));
        return appointmentMapper.toResponseList(list);
    }

    @Cacheable(value = "doctor-schedule", key = "#doctorId + '-' + #date")
    public List<AppointmentResponse> getDoctorSchedule(Long doctorId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Appointment> list = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(doctorId, start, end);
        return appointmentMapper.toResponseList(list);
    }

    public void validateAppointmentTime(LocalDateTime dateTime, Integer durationMinutes) {
        if (dateTime == null) throw new ValidationException("appointmentDateTime is required");
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now.plusHours(minAdvanceHours))) throw new ValidationException(String.format("Appointment must be booked at least %d hour(s) in advance", minAdvanceHours));
        if (dateTime.isAfter(now.plusDays(maxDaysInAdvance))) throw new ValidationException(String.format("Appointment cannot be booked more than %d days in advance", maxDaysInAdvance));
        int hour = dateTime.getHour();
        if (hour < businessHourStart || hour >= businessHourEnd) throw new ValidationException(String.format("Appointment must be within business hours: %02d:00-%02d:00", businessHourStart, businessHourEnd));
        java.time.DayOfWeek dow = dateTime.getDayOfWeek();
        if (!allowWeekendBooking && (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY)) throw new ValidationException("Appointments cannot be booked on weekends");
        if (durationMinutes == null || durationMinutes <= 0) throw new ValidationException("Invalid duration");
        if (allowedDurations != null && !allowedDurations.isEmpty()) {
            if (!allowedDurations.contains(durationMinutes)) throw new ValidationException("Duration is not one of allowed values: " + allowedDurations);
        } else {
            if (durationMinutes % 15 != 0) throw new ValidationException("Duration must be a multiple of 15 minutes");
        }
    }

    @SuppressWarnings("unused")
    @PostConstruct
    private void initBusinessRules() {
        try {
            LocalTime start = LocalTime.parse(businessHoursStartStr);
            LocalTime end = LocalTime.parse(businessHoursEndStr);
            businessHourStart = start.getHour();
            businessHourEnd = end.getHour();
        } catch (Exception e) {
            businessHourStart = 8;
            businessHourEnd = 18;
        }
        if (allowedDurationsStr != null && !allowedDurationsStr.trim().isEmpty()) {
            allowedDurations = java.util.Arrays.stream(allowedDurationsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::valueOf)
                    .collect(Collectors.toSet());
        }
    }

    public void checkDoctorAvailability(Long doctorId, LocalDateTime startTime, Integer durationMinutes) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        List<Appointment> conflicts = appointmentRepository.findOverlappingAppointments(doctorId, startTime, endTime);
        if (conflicts != null && !conflicts.isEmpty()) {
            log.warn("Doctor {} has {} conflicting appointments for time {}-{}", doctorId, conflicts.size(), startTime, endTime);
            try {
                schedulingConflictCounter.increment();
            } catch (Exception e) {
                log.debug("Failed to increment scheduling conflict counter: {}", e.getMessage());
            }
            throw new ValidationException("Doctor has conflicting appointment at this time");
        }
    }
}