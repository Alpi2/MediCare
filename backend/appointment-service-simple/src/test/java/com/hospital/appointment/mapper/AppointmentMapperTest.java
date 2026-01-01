package com.hospital.appointment.mapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import com.hospital.appointment.client.PatientServiceClient;
import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.AppointmentUpdateRequest;
import com.hospital.appointment.entity.Appointment;
import com.hospital.common.enums.AppointmentStatus;

@ExtendWith(MockitoExtension.class)
public class AppointmentMapperTest {

    @Mock
    private PatientServiceClient patientServiceClient;

    @InjectMocks
    private AppointmentMapper appointmentMapper;

    private AppointmentCreateRequest createRequest;
    private Appointment appointment;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        createRequest = AppointmentCreateRequest.builderFallbackInternal()
                .patientId(100L)
                .doctorId(10L)
                .appointmentDateTime(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .notes("Initial visit")
                .reason("Checkup")
                .build();

        appointment = new Appointment(
                55L,
                100L,
                10L,
                null,
                LocalDateTime.now().plusDays(1),
                AppointmentStatus.SCHEDULED,
                "Initial visit",
                "Checkup",
                30
        );
    }

    @Test
    void test_toEntity_shouldMapAllFields() {
        Appointment a = appointmentMapper.toEntity(createRequest);
        assertNotNull(a);
        assertNull(a.getId(), "Mapper should not set id");
        assertEquals(createRequest.getPatientId(), a.getPatientId());
        assertEquals(createRequest.getDoctorId(), a.getDoctorId());
        assertEquals(createRequest.getAppointmentDateTime(), a.getAppointmentDateTime());
        assertEquals(createRequest.getDurationMinutes(), a.getDurationMinutes());
        assertEquals(createRequest.getNotes(), a.getNotes());
        assertEquals(createRequest.getReason(), a.getReason());
    assertEquals(AppointmentStatus.SCHEDULED, a.getStatus(), "Default status should be SCHEDULED");
    }

    @Test
    void test_toResponse_shouldEnrichWithPatientName() {
        when(patientServiceClient.getPatientName(100L)).thenReturn("John Doe");

        AppointmentResponse resp = appointmentMapper.toResponse(appointment);

        assertNotNull(resp);
        assertEquals(appointment.getId(), resp.getId());
        assertEquals(appointment.getPatientId(), resp.getPatientId());
        assertEquals("John Doe", resp.getPatientName());
        assertEquals("Doctor #1", resp.getDoctorName(), "Expected placeholder doctor name");

        // computed fields
        assertFalse(resp.isPast());
        assertTrue(resp.isCanCancel());
        assertTrue(resp.isCanReschedule());

        verify(patientServiceClient, times(1)).getPatientName(100L);
    }

    @Test
    void test_toResponse_computedFields_futureAppointment() {
        Appointment future = appointment.toBuilder()
                .appointmentDateTime(LocalDateTime.now().plusDays(2))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientServiceClient.getPatientName(Objects.requireNonNull(future.getPatientId()))).thenReturn("Jane Doe");

        AppointmentResponse r = appointmentMapper.toResponse(future);
        assertFalse(r.isPast());
        assertTrue(r.isCanCancel());
        assertTrue(r.isCanReschedule());
    }

    @Test
    void test_toResponse_computedFields_pastAppointment() {
        Appointment past = appointment.toBuilder()
                .appointmentDateTime(LocalDateTime.now().minusDays(1))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientServiceClient.getPatientName(Objects.requireNonNull(past.getPatientId()))).thenReturn("Jane Doe");

        AppointmentResponse r = appointmentMapper.toResponse(past);
        assertTrue(r.isPast());
        assertFalse(r.isCanCancel());
    }

    @Test
    void test_toResponse_computedFields_completedStatus() {
        Appointment done = appointment.toBuilder()
                .appointmentDateTime(LocalDateTime.now().minusHours(2))
                .status(AppointmentStatus.COMPLETED)
                .build();

        when(patientServiceClient.getPatientName(Objects.requireNonNull(done.getPatientId()))).thenReturn("Jane Doe");

        AppointmentResponse r = appointmentMapper.toResponse(done);
        assertFalse(r.isCanCancel());
        assertFalse(r.isCanReschedule());
    }

    @Test
    void test_toResponse_patientServiceFailure() {
        when(patientServiceClient.getPatientName(100L)).thenThrow(new RestClientException("service down"));

        AppointmentResponse resp = appointmentMapper.toResponse(appointment);

        assertNotNull(resp);
        assertEquals("Patient #100", resp.getPatientName(), "Fallback patient name expected");
    }

    @Test
    void test_toResponseList_batchFetchPatientNames() {
        Appointment a1 = new Appointment(1L, 100L, 10L, null, appointment.getAppointmentDateTime(), appointment.getStatus(), appointment.getNotes(), appointment.getReason(), appointment.getDurationMinutes());
        Appointment a2 = new Appointment(2L, 101L, 10L, null, appointment.getAppointmentDateTime(), appointment.getStatus(), appointment.getNotes(), appointment.getReason(), appointment.getDurationMinutes());
        Appointment a3 = new Appointment(3L, 102L, 10L, null, appointment.getAppointmentDateTime(), appointment.getStatus(), appointment.getNotes(), appointment.getReason(), appointment.getDurationMinutes());

        List<Appointment> list = Arrays.asList(a1, a2, a3);
        List<Long> ids = list.stream().map(Appointment::getPatientId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Objects.requireNonNull(ids, "ids must not be null");

        Map<Long, String> names = Map.of(100L, "A", 101L, "B", 102L, "C");
        when(patientServiceClient.getPatientsByIds(ids)).thenReturn(names);

        List<AppointmentResponse> resp = appointmentMapper.toResponseList(list);

        verify(patientServiceClient, times(1)).getPatientsByIds(ids);
        assertEquals(3, resp.size());
        assertEquals("A", resp.get(0).getPatientName());
        assertEquals("B", resp.get(1).getPatientName());
        assertEquals("C", resp.get(2).getPatientName());
    }

    @Test
    void test_updateEntityFromRequest_shouldUpdateOnlyProvidedFields() {
        Appointment existing = appointment.toBuilder().notes("Old notes").appointmentDateTime(LocalDateTime.now().plusDays(3)).build();

        AppointmentUpdateRequest req = AppointmentUpdateRequest.builderFallbackInternal()
                .appointmentDateTime(LocalDateTime.now().plusDays(5))
                .notes("Updated notes")
                .build();

        appointmentMapper.updateEntityFromRequest(req, existing);

        assertEquals(req.getAppointmentDateTime(), existing.getAppointmentDateTime());
        assertEquals("Updated notes", existing.getNotes());
        // unchanged
        assertEquals(appointment.getPatientId(), existing.getPatientId());
        assertEquals(appointment.getDoctorId(), existing.getDoctorId());
    }

    @Test
    void test_updateEntityFromRequest_withNullValues() {
        Appointment existing = appointment.toBuilder().notes("Old notes").build();
        AppointmentUpdateRequest req = AppointmentUpdateRequest.builderFallbackInternal().build();

        appointmentMapper.updateEntityFromRequest(req, existing);

        assertEquals("Old notes", existing.getNotes());
    }
}
