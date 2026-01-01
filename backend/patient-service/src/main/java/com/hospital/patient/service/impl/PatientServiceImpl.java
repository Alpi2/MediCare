package com.hospital.patient.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.common.exception.DuplicateResourceException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.common.exception.ValidationException;
import com.hospital.common.kafka.KafkaProducerService;
import com.hospital.common.util.ValidationUtils;
import com.hospital.patient.domain.Patient;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.dto.PatientSearchCriteria;
import com.hospital.patient.dto.PatientUpdateRequest;
import com.hospital.patient.mapper.PatientMapper;
import com.hospital.patient.repository.PatientRepository;
import com.hospital.patient.service.PatientService;

@Service
@Transactional(readOnly = true)
public final class PatientServiceImpl implements PatientService {

  private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

  private final PatientRepository patientRepository;
  private final PatientMapper patientMapper;
  private final KafkaProducerService kafkaProducerService;
  private final io.micrometer.core.instrument.Counter patientRegistrationCounter;
  private final io.micrometer.core.instrument.Timer patientSearchTimer;

  // Explicit constructor replacing Lombok's @RequiredArgsConstructor
  public PatientServiceImpl(final PatientRepository patientRepository,
                            final PatientMapper patientMapper,
                            final KafkaProducerService kafkaProducerService,
                            final io.micrometer.core.instrument.Counter patientRegistrationCounter,
                            final io.micrometer.core.instrument.Timer patientSearchTimer) {
    this.patientRepository = patientRepository;
    this.patientMapper = patientMapper;
    this.kafkaProducerService = kafkaProducerService;
    this.patientRegistrationCounter = patientRegistrationCounter;
    this.patientSearchTimer = patientSearchTimer;
  }

  @Value("${kafka.topics.patient-events}")
  private String patientEventsTopic;
  /**
   * Create patient.
   * @param request Request.
   * @return Result.
   */

  @Override
  @Transactional
  @CacheEvict(value = "patients", allEntries = true)
  public PatientResponse createPatient(final @NonNull PatientCreateRequest request) {
    // Avoid logging sensitive fields (SSN, full request). Log only non-sensitive identifiers.
    log.debug("Creating patient: firstName={}, lastName={}, dateOfBirth={}",
    request.getFirstName(), request.getLastName(), request.getDateOfBirth());
    validatePatientData(request);

    String mrn = generateMedicalRecordNumber();
    Patient patient = patientMapper.toEntity(request);
    patient.setMedicalRecordNumber(mrn);

    String ssn = request.getSsn();
    if (ssn != null && existsBySSN(Objects.requireNonNull(ssn))) {
      throw new DuplicateResourceException("Patient", "ssn", ssn);
    }

    Patient saved = patientRepository.save(patient);
    PatientResponse response = patientMapper.toResponse(saved);

    // publish event asynchronously
    try {
      kafkaProducerService.sendEvent(patientEventsTopic, String.valueOf(saved.getId()), response);
    } catch (Exception e) {
      log.warn("Failed to publish patient.created event for id {}: {}", saved.getId(), e.getMessage());
    }

    log.info("Created patient id={} mrn={}", saved.getId(), saved.getMedicalRecordNumber());
    try {
      patientRegistrationCounter.increment();
    } catch (Exception e) {
      log.warn("Failed to increment patient registration counter: {}", e.getMessage());
    }
    return response;
  }
  /**
   * Update patient.
   * @param patientId Patient id.
   * @param request Request.
   * @return Result.
   */

  @Override
  @Transactional
  @Caching(evict = {
    @CacheEvict(value = "patients", key = "#patientId"),
    @CacheEvict(value = "patient-by-mrn", allEntries = true)
  })
  public PatientResponse updatePatient(final @NonNull Long patientId, final @NonNull PatientUpdateRequest request) {
    // Log only non-sensitive fields to avoid leaking PHI (do not log SSN or full payload)
    log.debug("Updating patient id={} firstName={} lastName={} dateOfBirth={}",
    patientId, request.getFirstName(), request.getLastName(), request.getDateOfBirth());
    Patient patient = Objects.requireNonNull(patientRepository.findById(patientId)
    .orElseThrow(() -> new ResourceNotFoundException("Patient not found")));

    patientMapper.updateEntityFromRequest(request, patient);
    Patient updated = patientRepository.save(patient);

    try {
      kafkaProducerService.sendEvent(patientEventsTopic, String.valueOf(updated.getId()), patientMapper.toResponse(updated));
    } catch (Exception e) {
      log.warn("Failed to publish patient.updated event for id {}: {}", updated.getId(), e.getMessage());
    }

    return patientMapper.toResponse(updated);
  }
  /**
   * Get patient by id.
   * @param patientId Patient id.
   * @return Result.
   */

  @Override
  @Cacheable(value = "patients", key = "#patientId")
  public Optional<PatientResponse> getPatientById(final @NonNull Long patientId) {
    return patientRepository.findById(patientId).map(patientMapper::toResponse);
  }
  /**
   * Get patient by mrn.
   * @param medicalRecordNumber Medical record number.
   * @return Result.
   */

  @Override
  @Cacheable(value = "patient-by-mrn", key = "#medicalRecordNumber")
  public Optional<PatientResponse> getPatientByMRN(final @NonNull String medicalRecordNumber) {
    ValidationUtils.validateNotEmpty(medicalRecordNumber, "medicalRecordNumber");
    return patientRepository.findByMedicalRecordNumber(medicalRecordNumber).map(patientMapper::toResponse);
  }
  /**
   * Get all patients.
   * @param pageable Pageable.
   * @return Result.
   */

  @Override
  public Page<PatientResponse> getAllPatients(final @NonNull Pageable pageable) {
    Page<Patient> page = patientRepository.findAll(pageable);
    return patientMapper.toResponsePage(page);
  }
  /**
   * Get active patients.
   * @param pageable Pageable.
   * @return Result.
   */

  @Override
  public Page<PatientResponse> getActivePatients(final @NonNull Pageable pageable) {
    Page<Patient> page = patientRepository.findActivePatients(pageable);
    return patientMapper.toResponsePage(page);
  }
  /**
   * Search patients.
   * @param criteria Criteria.
   * @param pageable Pageable.
   * @return Result.
   */

  @Override
  public Page<PatientResponse> searchPatients(final PatientSearchCriteria criteria, final @NonNull Pageable pageable) {
    long start = System.nanoTime();
    Page<Patient> page = patientRepository.advancedSearch(
    criteria.getFirstName(), criteria.getLastName(), criteria.getDateOfBirth(), criteria.getStatus(), pageable);
    try {
      patientSearchTimer.record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
    } catch (Exception t) {
      log.debug("Failed to record patient search timer: {}", t.getMessage());
    }
    return patientMapper.toResponsePage(page);
  }
  /**
   * Search patients by name.
   * @param searchTerm Search term.
   * @param pageable Pageable.
   * @return Result.
   */

  @Override
  public Page<PatientResponse> searchPatientsByName(final @NonNull String searchTerm, final @NonNull Pageable pageable) {
    ValidationUtils.validateNotEmpty(searchTerm, "searchTerm");
    Page<Patient> page = patientRepository.searchByName(searchTerm, pageable);
    return patientMapper.toResponsePage(page);
  }
  /**
   * Deactivate patient.
   * @param patientId Patient id.
   */

  @Override
  @Transactional
  @Caching(evict = {
    @CacheEvict(value = "patients", key = "#patientId"),
    @CacheEvict(value = "patient-by-mrn", allEntries = true)
  })
  public void deactivatePatient(final @NonNull Long patientId) {
    Patient p = patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    p.setStatus(Patient.PatientStatus.INACTIVE);
    patientRepository.save(p);
    try {
      kafkaProducerService.sendEvent(patientEventsTopic, String.valueOf(p.getId()), Map.of("event", "DEACTIVATED", "id", p.getId()));
    } catch (Exception e) {
      log.warn("Failed to publish patient.deactivated event for id {}: {}", p.getId(), e.getMessage());
    }
  }
  /**
   * Reactivate patient.
   * @param patientId Patient id.
   */

  @Override
  @Transactional
  @Caching(evict = {
    @CacheEvict(value = "patients", key = "#patientId"),
    @CacheEvict(value = "patient-by-mrn", allEntries = true)
  })
  public void reactivatePatient(final @NonNull Long patientId) {
    Patient p = patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    p.setStatus(Patient.PatientStatus.ACTIVE);
    patientRepository.save(p);
    try {
      kafkaProducerService.sendEvent(patientEventsTopic, String.valueOf(p.getId()), Map.of("event", "REACTIVATED", "id", p.getId()));
    } catch (Exception e) {
      log.warn("Failed to publish patient.reactivated event for id {}: {}", p.getId(), e.getMessage());
    }
  }
  /**
   * Delete patient.
   * @param patientId Patient id.
   */

  @Override
  @Transactional
  @CacheEvict(value = {"patients", "patient-by-mrn"}, allEntries = true)
  public void deletePatient(final @NonNull Long patientId) {
    // Ensure patient exists
    patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    patientRepository.deleteById(patientId);
    try {
      kafkaProducerService.sendEvent(patientEventsTopic, String.valueOf(patientId), Map.of("event", "DELETED", "id", patientId));
    } catch (Exception e) {
      log.warn("Failed to publish patient.deleted event for id {}: {}", patientId, e.getMessage());
    }
  }
  /**
   * Get patients with allergies.
   * @return Result.
   */

  @Override
  public List<PatientResponse> getPatientsWithAllergies() {
    return patientMapper.toResponseList(patientRepository.findPatientsWithAllergies());
  }
  /**
   * Get patients by medical condition.
   * @param condition Condition.
   * @return Result.
   */

  @Override
  public List<PatientResponse> getPatientsByMedicalCondition(final @NonNull String condition) {
    ValidationUtils.validateNotEmpty(condition, "condition");
    return patientMapper.toResponseList(patientRepository.findByMedicalCondition(condition));
  }
  /**
   * Get patients by insurance provider.
   * @param provider Provider.
   * @return Result.
   */

  @Override
  public List<PatientResponse> getPatientsByInsuranceProvider(final @NonNull String provider) {
    ValidationUtils.validateNotEmpty(provider, "provider");
    return patientMapper.toResponseList(patientRepository.findByInsuranceProviderIgnoreCase(provider));
  }
  /**
   * Get patient statistics.
   * @return Result.
   */

  @Override
  @Cacheable(value = "patient-statistics", key = "'stats'")
  public PatientService.PatientStatistics getPatientStatistics() {
    long total = patientRepository.count();
    long active = patientRepository.countActivePatients();
    long inactive = patientRepository.countByStatus(Patient.PatientStatus.INACTIVE);
    int registeredToday = patientRepository.findPatientsRegisteredToday().size();
    int patientsWithAllergies = patientRepository.findPatientsWithAllergies().size();
    int patientsWithConditions = (int) patientRepository.countPatientsWithMedicalConditions();

    PatientService.PatientStatistics stats = new PatientService.PatientStatistics(total, active, inactive, registeredToday, patientsWithAllergies, patientsWithConditions);
    return stats;
  }
  /**
   * Generate medical record number.
   * @return Result.
   */

  @Override
  public String generateMedicalRecordNumber() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
    String date = LocalDate.now().format(dtf);
    // Deterministic per-day sequence: use today's registered count as a starting point
    int todayCount;
    try {
      todayCount = patientRepository.findPatientsRegisteredToday().size();
    } catch (Exception e) {
      // If repository method fails for some reason, fallback to 0 and continue — log for observability
      log.warn("Failed to read today's patient count for MRN generation: {}. Falling back to random start.", e.getMessage());
      todayCount = 0;
    }

    // Start sequence at count+1 for today and attempt forward if collisions occur.
    int startSeq = todayCount + 1;
    final int maxAttempts = 1000; // allow many attempts to handle races
    for (int attempt = 0; attempt < maxAttempts; attempt++) {
      int seq = startSeq + attempt;
      String suffix = String.format("%04d", seq % 10000); // wrap if exceeding 9999
      String mrn = String.format("MRN-%s-%s", date, suffix);
      if (!existsByMedicalRecordNumber(Objects.requireNonNull(mrn))) {
        if (attempt > 0) {
          log.info("Generated MRN {} after {} additional attempts due to collisions", mrn, attempt);
        }
        return mrn;
      }
      // Log collision for observability and continue to next sequence
      log.warn("MRN collision detected while generating MRN: {} (attempt {}). Trying next sequence.", mrn, attempt + 1);
    }

    // As a last resort, fall back to randomized suffixes but log prominently
    log.error("Exceeded {} attempts generating deterministic MRN for date {}. Falling back to random generation.", maxAttempts, date);
    for (int i = 0; i < 50; i++) {
      String suffix = String.format("%04d", (int) (Math.random() * 10000));
      String mrn = String.format("MRN-%s-%s", date, suffix);
      if (!existsByMedicalRecordNumber(Objects.requireNonNull(mrn))) {
        log.warn("Generated MRN {} using fallback random strategy after deterministic attempts failed", mrn);
        return mrn;
      }
    }
    throw new ValidationException("Unable to generate unique MRN after deterministic and fallback attempts");
  }
  /**
   * Validate patient data.
   * @param request Request.
   */

  @Override
  public void validatePatientData(final @NonNull PatientCreateRequest request) {
    if (request.getEmail() != null && !ValidationUtils.isValidEmail(request.getEmail())) {
      throw new ValidationException("Invalid email");
    }
    if (request.getPhoneNumber() != null && !ValidationUtils.isValidPhoneNumber(request.getPhoneNumber())) {
      throw new ValidationException("Invalid phone number");
    }
    if (request.getSsn() != null && !ValidationUtils.isValidSSN(request.getSsn())) {
      throw new ValidationException("Invalid SSN");
    }
    if (request.getDateOfBirth() != null && request.getDateOfBirth().isAfter(LocalDate.now())) {
      throw new ValidationException("Date of birth cannot be in the future");
    }
  }
  /**
   * Exists by medical record number.
   * @param mrn Mrn.
   * @return Result.
   */

  @Override
  public boolean existsByMedicalRecordNumber(final @NonNull String mrn) {
    return patientRepository.existsByMedicalRecordNumber(mrn);
  }
  /**
   * Exists by ssn.
   * @param ssn Ssn.
   * @return Result.
   */

  @Override
  public boolean existsBySSN(final @NonNull String ssn) {
    return patientRepository.existsBySsn(ssn);
  }
}
