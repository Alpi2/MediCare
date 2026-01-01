package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Appointment;
import com.hospital.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    // Count appointments by status (useful for metrics)
    Long countByStatus(AppointmentStatus status);

    Long countByDoctorIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

        List<Appointment> findByDoctorIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    // Used for upcoming appointments for a patient; accept multiple statuses and a Sort param
    List<Appointment> findByPatientIdAndAppointmentDateTimeAfterAndStatusIn(Long patientId, LocalDateTime after, Iterable<com.hospital.common.enums.AppointmentStatus> statuses, org.springframework.data.domain.Sort sort);

    @Query(value = "SELECT * FROM appointments a " +
        "WHERE a.doctor_id = :doctorId " +
        "AND a.status NOT IN ('CANCELLED','NO_SHOW') " +
        "AND (a.appointment_date_time < :end AND (a.appointment_date_time + (COALESCE(a.duration_minutes,0) || ' minutes')::interval) > :start)", nativeQuery = true)
    List<Appointment> findOverlappingAppointments(@Param("doctorId") Long doctorId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}