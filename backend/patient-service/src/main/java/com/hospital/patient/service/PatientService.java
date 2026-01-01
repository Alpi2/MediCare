package com.hospital.patient.service;

import com.hospital.patient.domain.Patient;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.dto.PatientSearchCriteria;
import com.hospital.patient.dto.PatientUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
* Patient Service Interface
*
* Defines business operations for patient management.
* Implements hospital-specific patient workflows.
*/
public interface PatientService {

  /**
  * Create a new patient
  */
  PatientResponse createPatient(@NonNull PatientCreateRequest request);

  /**
  * Update existing patient
  */
  PatientResponse updatePatient(@NonNull Long patientId, @NonNull PatientUpdateRequest request);

  /**
  * Get patient by ID
  */
  Optional<PatientResponse> getPatientById(@NonNull Long patientId);

  /**
  * Get patient by medical record number
  */
  Optional<PatientResponse> getPatientByMRN(@NonNull String medicalRecordNumber);

  /**
  * Get all patients with pagination
  */
  Page<PatientResponse> getAllPatients(@NonNull Pageable pageable);

  /**
  * Get active patients
  */
  Page<PatientResponse> getActivePatients(@NonNull Pageable pageable);

  /**
  * Search patients by criteria
  */
  Page<PatientResponse> searchPatients(PatientSearchCriteria criteria, @NonNull Pageable pageable);

  /**
  * Search patients by name
  */
  Page<PatientResponse> searchPatientsByName(@NonNull String searchTerm, @NonNull Pageable pageable);

  /**
  * Deactivate patient (soft delete)
  */
  void deactivatePatient(@NonNull Long patientId);

  /**
  * Reactivate patient
  */
  void reactivatePatient(@NonNull Long patientId);

  /**
  * Delete patient (hard delete - admin only)
  */
  void deletePatient(@NonNull Long patientId);

  /**
  * Get patients with allergies
  */
  List<PatientResponse> getPatientsWithAllergies();

  /**
  * Get patients by medical condition
  */
  List<PatientResponse> getPatientsByMedicalCondition(@NonNull String condition);

  /**
  * Get patients by insurance provider
  */
  List<PatientResponse> getPatientsByInsuranceProvider(@NonNull String provider);

  /**
  * Get patient statistics
  */
  PatientStatistics getPatientStatistics();

  /**
  * Generate new medical record number
  */
  String generateMedicalRecordNumber();

  /**
  * Validate patient data
  */
  void validatePatientData(@NonNull PatientCreateRequest request);

  /**
  * Check if patient exists by MRN
  */
  boolean existsByMedicalRecordNumber(@NonNull String mrn);

  /**
  * Check if patient exists by SSN
  */
  boolean existsBySSN(@NonNull String ssn);

  /**
  * Patient Statistics DTO
  */
  record PatientStatistics(
  long totalPatients,
  long activePatients,
  long inactivePatients,
  long patientsRegisteredToday,
  long patientsWithAllergies,
  long patientsWithMedicalConditions
  ) {}
}
