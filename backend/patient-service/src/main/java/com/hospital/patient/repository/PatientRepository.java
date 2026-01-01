package com.hospital.patient.repository;

import com.hospital.patient.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
* Patient Repository
*
* Data access layer for Patient entity with custom queries
* for hospital-specific operations.
*/
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>,    JpaSpecificationExecutor<Patient> {

  /**
  * Find patient by medical record number
  */
  Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber);

  /**
  * Find patient by SSN (encrypted field)
  */
  Optional<Patient> findBySsn(String ssn);

  /**
  * Find patients by email
  */
  List<Patient> findByEmailIgnoreCase(String email);

  /**
  * Find patients by phone number
  */
  List<Patient> findByPhoneNumber(String phoneNumber);

  /**
  * Find patients by status
  */
  Page<Patient> findByStatus(Patient.PatientStatus status, Pageable pageable);

  /**
  * Find active patients
  */
  @Query("SELECT p FROM Patient p WHERE p.status = 'ACTIVE'")
  Page<Patient> findActivePatients(Pageable pageable);

  /**
  * Search patients by name (first, middle, last)
  */
  @Query("SELECT p FROM Patient p WHERE " +
  "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
  "LOWER(p.middleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
  "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Patient> searchByName(@Param("searchTerm") String searchTerm, Pageable      pageable);

  /**
  * Find patients by date of birth range
  */
  @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND      :endDate")
  List<Patient> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate      ,
  @Param("endDate") LocalDate endDate);

  /**
  * Find patients by age range
  */
  @Query("SELECT p FROM Patient p WHERE " +
  "YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
  List<Patient> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int      maxAge);

  /**
  * Find patients with allergies
  */
  @Query("SELECT p FROM Patient p WHERE p.allergies IS NOT NULL AND p.allergies      != ''")
  List<Patient> findPatientsWithAllergies();

  /**
  * Find patients with specific medical conditions
  */
  @Query("SELECT p FROM Patient p WHERE LOWER(p.medicalConditions) LIKE      LOWER(CONCAT('%', :condition, '%'))")
  List<Patient> findByMedicalCondition(@Param("condition") String condition);

  /**
  * Find patients by insurance provider
  */
  List<Patient> findByInsuranceProviderIgnoreCase(String insuranceProvider);

  /**
  * Find patients by city
  */
  @Query("SELECT p FROM Patient p WHERE LOWER(p.address.city) = LOWER(:city)")
  List<Patient> findByCity(@Param("city") String city);

  /**
  * Find patients by state/province
  */
  @Query("SELECT p FROM Patient p WHERE LOWER(p.address.stateProvince) =      LOWER(:state)")
  List<Patient> findByState(@Param("state") String state);

  /**
  * Count patients by status
  */
  long countByStatus(Patient.PatientStatus status);

  /**
  * Count active patients
  *
  * Return Long (object) to avoid issues with nullability in some query      providers
  */
  @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = 'ACTIVE'")
  Long countActivePatients();

  /**
  * Find patients registered today
  */
  @Query("SELECT p FROM Patient p WHERE DATE(p.createdDate) = CURRENT_DATE")
  List<Patient> findPatientsRegisteredToday();

  /**
  * Find patients with recent activity (last 30 days)
  */
  @Query("SELECT p FROM Patient p WHERE p.lastModifiedDate >= CURRENT_DATE -      30")
  List<Patient> findPatientsWithRecentActivity();

  /**
  * Count patients which have medical conditions recorded (non-null and      non-empty)
  */
  @Query("SELECT COUNT(p) FROM Patient p WHERE p.medicalConditions IS NOT NULL      AND TRIM(p.medicalConditions) <> ''")
  long countPatientsWithMedicalConditions();

  /**
  * Advanced search with multiple criteria
  */
  @Query("SELECT p FROM Patient p WHERE " +
  "(:firstName IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName,      '%'))) AND " +
  "(:lastName IS NULL OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName,      '%'))) AND " +
  "(:dateOfBirth IS NULL OR p.dateOfBirth = :dateOfBirth) AND " +
  "(:status IS NULL OR p.status = :status)")
  Page<Patient> advancedSearch(@Param("firstName") String firstName,
  @Param("lastName") String lastName,
  @Param("dateOfBirth") LocalDate dateOfBirth,
  @Param("status") Patient.PatientStatus status,
  Pageable pageable);

  /**
  * Check if medical record number exists
  */
  boolean existsByMedicalRecordNumber(String medicalRecordNumber);

  /**
  * Check if SSN exists
  */
  boolean existsBySsn(String ssn);
}
