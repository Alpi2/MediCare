package com.hospital.patient.repository;

import com.hospital.patient.domain.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PatientRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Patient buildPatient(String mrn, String firstName, String lastName, LocalDate dob, Patient.PatientStatus status,
                                 String allergies, String medicalConditions, String ssn, LocalDateTime createdDate) {
        Patient p = Patient.builder()
                .medicalRecordNumber(mrn)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dob)
                .gender(com.hospital.common.enums.Gender.MALE)
                .status(status)
                .allergies(allergies)
                .medicalConditions(medicalConditions)
                .ssn(ssn)
                .createdDate(createdDate)
                .build();
        return p;
    }

    @Test
    @DisplayName("test_findByMedicalRecordNumber - returns patient when MRN exists and empty when not")
    public void test_findByMedicalRecordNumber() {
        String mrn = "MRN-20240101-0001";
        Patient p = buildPatient(mrn, "Alice", "One", LocalDate.of(1990,1,1), Patient.PatientStatus.ACTIVE,
                null, null, "123-45-6789", LocalDateTime.now());
        entityManager.persistAndFlush(p);

        Optional<Patient> found = patientRepository.findByMedicalRecordNumber(mrn);
        assertTrue(found.isPresent());
        assertEquals(mrn, found.get().getMedicalRecordNumber());

        Optional<Patient> notFound = patientRepository.findByMedicalRecordNumber("MRN-19000101-9999");
        assertFalse(notFound.isPresent());
    }

    @Test
    @DisplayName("test_findActivePatients - returns only ACTIVE patients")
    public void test_findActivePatients() {
        // create 3 active
        for (int i = 0; i < 3; i++) {
            Patient p = buildPatient("MRN-20250101-00" + i, "Active", "Patient" + i, LocalDate.of(1980,1,1), Patient.PatientStatus.ACTIVE,
                    null, null, null, LocalDateTime.now());
            entityManager.persist(p);
        }
        // create 2 inactive
        for (int i = 0; i < 2; i++) {
            Patient p = buildPatient("MRN-20250102-00" + i, "Inactive", "Patient" + i, LocalDate.of(1970,1,1), Patient.PatientStatus.INACTIVE,
                    null, null, null, LocalDateTime.now());
            entityManager.persist(p);
        }
        entityManager.flush();

        Page<Patient> page = patientRepository.findActivePatients(Pageable.unpaged());
        assertEquals(3, page.getContent().size());
        for (Patient p : page.getContent()) {
            assertEquals(Patient.PatientStatus.ACTIVE, p.getStatus());
        }
    }

    @Test
    @DisplayName("test_searchByName - case-insensitive search by last name")
    public void test_searchByName() {
        Patient p1 = buildPatient("MRN-20250103-0001", "John", "Doe", LocalDate.of(1985,5,5), Patient.PatientStatus.ACTIVE, null, null, null, LocalDateTime.now());
        Patient p2 = buildPatient("MRN-20250103-0002", "Jane", "Doe", LocalDate.of(1990,6,6), Patient.PatientStatus.ACTIVE, null, null, null, LocalDateTime.now());
        Patient p3 = buildPatient("MRN-20250103-0003", "Bob", "Smith", LocalDate.of(1975,3,3), Patient.PatientStatus.ACTIVE, null, null, null, LocalDateTime.now());
        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();

        Page<Patient> result1 = patientRepository.searchByName("Doe", Pageable.unpaged());
        assertEquals(2, result1.getContent().size());

        Page<Patient> result2 = patientRepository.searchByName("doe", Pageable.unpaged());
        assertEquals(2, result2.getContent().size());
    }

    @Test
    @DisplayName("test_findPatientsWithAllergies - returns only non-null/non-empty allergies")
    public void test_findPatientsWithAllergies() {
        Patient pAllergy = buildPatient("MRN-20250104-0001", "Allergy", "One", LocalDate.of(1982,2,2), Patient.PatientStatus.ACTIVE,
                "Peanuts", null, null, LocalDateTime.now());
        Patient pNull = buildPatient("MRN-20250104-0002", "NoAllergy", "Null", LocalDate.of(1983,3,3), Patient.PatientStatus.ACTIVE,
                null, null, null, LocalDateTime.now());
        Patient pEmpty = buildPatient("MRN-20250104-0003", "NoAllergy", "Empty", LocalDate.of(1984,4,4), Patient.PatientStatus.ACTIVE,
                "", null, null, LocalDateTime.now());
        entityManager.persist(pAllergy);
        entityManager.persist(pNull);
        entityManager.persist(pEmpty);
        entityManager.flush();

        List<Patient> withAllergies = patientRepository.findPatientsWithAllergies();
        assertEquals(1, withAllergies.size());
        assertEquals("Peanuts", withAllergies.get(0).getAllergies());
    }

    @Test
    @DisplayName("test_findByMedicalCondition - partial, case-insensitive match")
    public void test_findByMedicalCondition() {
        Patient p = buildPatient("MRN-20250105-0001", "Cond", "One", LocalDate.of(1970,7,7), Patient.PatientStatus.ACTIVE,
                null, "Diabetes, Hypertension", null, LocalDateTime.now());
        entityManager.persistAndFlush(p);

        List<Patient> results = patientRepository.findByMedicalCondition("Diabetes");
        assertEquals(1, results.size());
        assertTrue(results.get(0).getMedicalConditions().toLowerCase().contains("diabetes"));
    }

    @Test
    @DisplayName("test_countActivePatients - returns the correct count")
    public void test_countActivePatients() {
        for (int i = 0; i < 5; i++) {
            entityManager.persist(buildPatient("MRN-20250106-0" + i, "A", "Act" + i, LocalDate.of(1990,1,1), Patient.PatientStatus.ACTIVE,
                    null, null, null, LocalDateTime.now()));
        }
        for (int i = 0; i < 3; i++) {
            entityManager.persist(buildPatient("MRN-20250107-0" + i, "B", "Inact" + i, LocalDate.of(1980,1,1), Patient.PatientStatus.INACTIVE,
                    null, null, null, LocalDateTime.now()));
        }
        entityManager.flush();

        Long activeCount = patientRepository.countActivePatients();
        assertNotNull(activeCount);
        assertEquals(5L, activeCount.longValue());
    }

    @Test
    @DisplayName("test_findPatientsRegisteredToday - returns only today's patients")
    public void test_findPatientsRegisteredToday() {
        Patient today = buildPatient("MRN-20250108-0001", "Today", "User", LocalDate.of(2000,1,1), Patient.PatientStatus.ACTIVE,
                null, null, null, LocalDateTime.now());
        Patient yesterday = buildPatient("MRN-20250108-0002", "Yesterday", "User", LocalDate.of(2000,1,1), Patient.PatientStatus.ACTIVE,
                null, null, null, LocalDateTime.now().minusDays(1));
        entityManager.persist(today);
        entityManager.persist(yesterday);
        entityManager.flush();

        List<Patient> results = patientRepository.findPatientsRegisteredToday();
        assertEquals(1, results.size());
        assertEquals("MRN-20250108-0001", results.get(0).getMedicalRecordNumber());
    }

    @Test
    @DisplayName("test_advancedSearch_multiplecriteria - filters correctly and returns all when criteria null")
    public void test_advancedSearch_multiplecriteria() {
        Patient johnActive = buildPatient("MRN-20250109-0001", "John", "Doe", LocalDate.of(1990,1,1), Patient.PatientStatus.ACTIVE,
                null, null, null, LocalDateTime.now());
        Patient johnInactive = buildPatient("MRN-20250109-0002", "John", "Smith", LocalDate.of(1990,1,1), Patient.PatientStatus.INACTIVE,
                null, null, null, LocalDateTime.now());
        entityManager.persist(johnActive);
        entityManager.persist(johnInactive);
        entityManager.flush();

        Page<Patient> filtered = patientRepository.advancedSearch("John", null, null, Patient.PatientStatus.ACTIVE, Pageable.unpaged());
        assertTrue(filtered.getContent().stream().allMatch(p -> p.getFirstName().equalsIgnoreCase("John")));
        assertTrue(filtered.getContent().stream().allMatch(p -> p.getStatus() == Patient.PatientStatus.ACTIVE));

        // All null criteria -> should return at least the two created in this test (plus any others in the transactional context)
        Page<Patient> all = patientRepository.advancedSearch(null, null, null, null, Pageable.unpaged());
        assertTrue(all.getTotalElements() >= 2);
    }

    @Test
    @DisplayName("test_existsByMedicalRecordNumber - returns true/false appropriately")
    public void test_existsByMedicalRecordNumber() {
        String mrn = "MRN-20250110-0001";
        entityManager.persistAndFlush(buildPatient(mrn, "Exist", "Mrn", LocalDate.of(1995,5,5), Patient.PatientStatus.ACTIVE,
                null, null, null, LocalDateTime.now()));

        assertTrue(patientRepository.existsByMedicalRecordNumber(mrn));
        assertFalse(patientRepository.existsByMedicalRecordNumber("MRN-19000101-9999"));
    }

    @Test
    @DisplayName("test_existsBySsn - returns true/false appropriately")
    public void test_existsBySsn() {
        String ssn = "999-88-7777";
        entityManager.persistAndFlush(buildPatient("MRN-20250111-0001", "Exist", "Ssn", LocalDate.of(1995,5,5), Patient.PatientStatus.ACTIVE,
                null, null, ssn, LocalDateTime.now()));

        assertTrue(patientRepository.existsBySsn(ssn));
        assertFalse(patientRepository.existsBySsn("000-00-0000"));
    }
}
