package com.hospital.appointment.mapper;

import com.hospital.appointment.client.PatientServiceClient;
import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.AppointmentUpdateRequest;
import com.hospital.appointment.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AppointmentMapper.class);

    private final PatientServiceClient patientServiceClient;

    // Explicit constructor to satisfy IDEs that do not run Lombok processors
    public AppointmentMapper(PatientServiceClient patientServiceClient) {
        this.patientServiceClient = patientServiceClient;
    }

    public Appointment toEntity(AppointmentCreateRequest req) {
        Appointment a = new Appointment();
        a.setPatientId(req.getPatientId());
        a.setDoctorId(req.getDoctorId());
        a.setDepartmentId(req.getDepartmentId());
        a.setAppointmentDateTime(req.getAppointmentDateTime());
        a.setDurationMinutes(req.getDurationMinutes());
        a.setNotes(req.getNotes());
        a.setReason(req.getReason());
        // status defaults to SCHEDULED in entity
        return a;
    }

    public AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse resp = new AppointmentResponse();
        resp.setId(a.getId());
        resp.setPatientId(a.getPatientId());
        resp.setPatientName(patientServiceClient.getPatientName(Objects.requireNonNull(a.getPatientId())));
        resp.setDoctorId(a.getDoctorId());
        resp.setDoctorName("Doctor #" + a.getDoctorId());
        resp.setDepartmentId(a.getDepartmentId());
        resp.setAppointmentDateTime(a.getAppointmentDateTime());
        resp.setStatus(a.getStatus());
        resp.setNotes(a.getNotes());
        resp.setReason(a.getReason());
        resp.setDurationMinutes(a.getDurationMinutes());
        resp.setCreatedAt(a.getCreatedAt());
        resp.setUpdatedAt(a.getUpdatedAt());

        LocalDateTime now = LocalDateTime.now();
        resp.setIsPast(a.getAppointmentDateTime() != null && a.getAppointmentDateTime().isBefore(now));
        boolean schedOrConf = a.getStatus() == null ? false : (a.getStatus().name().equals("SCHEDULED") || a.getStatus().name().equals("CONFIRMED"));
        resp.setCanCancel(schedOrConf && a.getAppointmentDateTime() != null && a.getAppointmentDateTime().isAfter(now));
        resp.setCanReschedule(schedOrConf);
        return resp;
    }

    public List<AppointmentResponse> toResponseList(List<Appointment> list) {
        // batch fetch patient names
        Map<Long, String> names = patientServiceClient.getPatientsByIds(list.stream().map(Appointment::getPatientId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        Objects.requireNonNull(names, "names must not be null");
        return list.stream().map(a -> {
            AppointmentResponse r = toResponse(a);
            // override patientName with batch result if present
            Long pid = a.getPatientId();
            if (pid != null && names.containsKey(pid)) r.setPatientName(names.get(pid));
            return r;
        }).collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<AppointmentResponse> toResponsePage(org.springframework.data.domain.Page<Appointment> page) {
        return page.map(this::toResponse);
    }

    public void updateEntityFromRequest(AppointmentUpdateRequest req, Appointment appointment) {
    if (req.getAppointmentDateTime() != null) appointment.setAppointmentDateTime(req.getAppointmentDateTime());
        if (req.getDurationMinutes() != null) appointment.setDurationMinutes(req.getDurationMinutes());
        if (req.getNotes() != null) appointment.setNotes(req.getNotes());
    if (req.getReason() != null) appointment.setReason(req.getReason());
    }
}
