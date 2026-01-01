package com.hospital.appointment.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.hospital.appointment.entity.Appointment;
import com.hospital.common.enums.AppointmentStatus;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AppointmentRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    /**
     * Registers dynamic DB properties for Testcontainers.
     * Invoked reflectively by Spring; intentionally not referenced directly.
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Appointment buildAppointment(Long patientId, Long doctorId, LocalDateTime dt, Integer durationMinutes, AppointmentStatus status) {
        return new Appointment(
                null,
                patientId,
                doctorId,
                null,
                dt,
                status == null ? AppointmentStatus.SCHEDULED : status,
                null,
                null,
                durationMinutes
        );
    }

    @Test
    @DisplayName("test_findByPatientId - returns correct number of appointments for a patient")
    public void test_findByPatientId() {
        entityManager.persist(buildAppointment(1L, 10L, LocalDateTime.now(), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(1L, 11L, LocalDateTime.now().plusDays(1), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(1L, 12L, LocalDateTime.now().plusDays(2), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(2L, 10L, LocalDateTime.now(), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(2L, 11L, LocalDateTime.now().plusDays(1), 30, AppointmentStatus.SCHEDULED));
        entityManager.flush();

        List<Appointment> results = appointmentRepository.findByPatientId(1L);
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("test_findByDoctorId - returns only doctor's appointments")
    public void test_findByDoctorId() {
        entityManager.persist(buildAppointment(1L, 1L, LocalDateTime.now(), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(2L, 1L, LocalDateTime.now().plusDays(1), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(3L, 2L, LocalDateTime.now().plusDays(2), 30, AppointmentStatus.SCHEDULED));
        entityManager.flush();

        List<Appointment> d1 = appointmentRepository.findByDoctorId(1L);
        assertEquals(2, d1.size());
        for (Appointment a : d1) assertEquals(1L, a.getDoctorId());
    }

    @Test
    @DisplayName("test_findByStatus - filters by status")
    public void test_findByStatus() {
        entityManager.persist(buildAppointment(1L, 1L, LocalDateTime.now(), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(2L, 1L, LocalDateTime.now().plusDays(1), 30, AppointmentStatus.COMPLETED));
        entityManager.persist(buildAppointment(3L, 2L, LocalDateTime.now().plusDays(2), 30, AppointmentStatus.SCHEDULED));
        entityManager.flush();

        List<Appointment> scheduled = appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED);
        assertEquals(2, scheduled.size());
        for (Appointment a : scheduled) assertEquals(AppointmentStatus.SCHEDULED, a.getStatus());
    }

    @Test
    @DisplayName("test_countByStatus - returns correct counts")
    public void test_countByStatus() {
        entityManager.persist(buildAppointment(1L, 1L, LocalDateTime.now(), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(2L, 1L, LocalDateTime.now().plusDays(1), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(3L, 2L, LocalDateTime.now().plusDays(2), 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(4L, 2L, LocalDateTime.now().plusDays(3), 30, AppointmentStatus.COMPLETED));
        entityManager.persist(buildAppointment(5L, 3L, LocalDateTime.now().plusDays(4), 30, AppointmentStatus.COMPLETED));
        entityManager.persist(buildAppointment(6L, 3L, LocalDateTime.now().plusDays(5), 30, AppointmentStatus.CANCELLED));
        entityManager.flush();

        Long scheduledCount = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED);
        assertEquals(3L, scheduledCount.longValue());
    }

    @Test
    @DisplayName("test_findOverlappingAppointments_detectsConflict - detects overlapping appointment")
    public void test_findOverlappingAppointments_detectsConflict() {
        LocalDateTime start = LocalDate.now().atTime(10,0);
        Appointment existing = buildAppointment(1L, 1L, start, 30, AppointmentStatus.SCHEDULED);
        entityManager.persist(existing);
        entityManager.flush();

        LocalDateTime queryStart = start.plusMinutes(15);
        LocalDateTime queryEnd = start.plusMinutes(45);

        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(1L, queryStart, queryEnd);
        assertEquals(1, overlaps.size());
        assertEquals(existing.getDoctorId(), overlaps.get(0).getDoctorId());
    }

    @Test
    @DisplayName("test_findOverlappingAppointments_noConflict - adjacent appointments do not count as overlap")
    public void test_findOverlappingAppointments_noConflict() {
        LocalDateTime start = LocalDate.now().atTime(10,0);
        Appointment existing = buildAppointment(1L, 1L, start, 30, AppointmentStatus.SCHEDULED);
        entityManager.persist(existing);
        entityManager.flush();

        LocalDateTime queryStart = start.plusMinutes(30);
        LocalDateTime queryEnd = start.plusMinutes(60);

        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(1L, queryStart, queryEnd);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @DisplayName("test_findOverlappingAppointments_ignoresCancelled - cancelled appointments are ignored")
    public void test_findOverlappingAppointments_ignoresCancelled() {
        LocalDateTime start = LocalDate.now().atTime(10,0);
        Appointment cancelled = buildAppointment(1L, 1L, start, 30, AppointmentStatus.CANCELLED);
        entityManager.persist(cancelled);
        entityManager.flush();

        LocalDateTime queryStart = start.plusMinutes(15);
        LocalDateTime queryEnd = start.plusMinutes(45);

        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(1L, queryStart, queryEnd);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @DisplayName("test_findByDoctorIdAndAppointmentDateTimeBetween - date range returns correct appointments")
    public void test_findByDoctorIdAndAppointmentDateTimeBetween() {
        LocalDateTime d1 = LocalDate.now().atTime(9,0);
        LocalDateTime d2 = LocalDate.now().atTime(11,0);
        LocalDateTime d3 = LocalDate.now().plusDays(1).atTime(10,0);

        entityManager.persist(buildAppointment(1L, 1L, d1, 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(2L, 1L, d2, 30, AppointmentStatus.SCHEDULED));
        entityManager.persist(buildAppointment(3L, 1L, d3, 30, AppointmentStatus.SCHEDULED));
        entityManager.flush();

        List<Appointment> results = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(1L, LocalDate.now().atStartOfDay(), LocalDate.now().atTime(23,59));
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("test_findByPatientIdAndAppointmentDateTimeAfterAndStatusIn - upcoming appointments filtered and sorted")
    public void test_findByPatientIdAndAppointmentDateTimeAfterAndStatusIn() {
        LocalDateTime now = LocalDateTime.now();
        Appointment past = buildAppointment(1L, 1L, now.minusDays(2), 30, AppointmentStatus.SCHEDULED);
        Appointment future1 = buildAppointment(1L, 1L, now.plusDays(1), 30, AppointmentStatus.SCHEDULED);
        Appointment future2 = buildAppointment(1L, 1L, now.plusDays(2), 30, AppointmentStatus.CONFIRMED);
        Appointment future3 = buildAppointment(1L, 1L, now.plusDays(3), 30, AppointmentStatus.CANCELLED);
        entityManager.persist(past);
        entityManager.persist(future1);
        entityManager.persist(future2);
        entityManager.persist(future3);
        entityManager.flush();

        List<Appointment> upcoming = appointmentRepository.findByPatientIdAndAppointmentDateTimeAfterAndStatusIn(1L, now, Arrays.asList(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED), org.springframework.data.domain.Sort.by("appointmentDateTime"));
        assertEquals(2, upcoming.size());
        assertTrue(upcoming.get(0).getAppointmentDateTime().isBefore(upcoming.get(1).getAppointmentDateTime()));
    }

    @Test
    @DisplayName("test_countByDoctorIdAndAppointmentDateTimeBetween - counts appointments for doctor in range")
    public void test_countByDoctorIdAndAppointmentDateTimeBetween() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            entityManager.persist(buildAppointment((long) i, 1L, today.atTime(9 + i, 0), 30, AppointmentStatus.SCHEDULED));
        }
        entityManager.flush();

        Long count = appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(1L, today.atStartOfDay(), today.atTime(23,59));
        assertEquals(5L, count.longValue());
    }
}
