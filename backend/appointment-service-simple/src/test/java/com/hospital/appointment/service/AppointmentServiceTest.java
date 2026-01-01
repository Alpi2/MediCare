package com.hospital.appointment.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable; 
import org.springframework.test.util.ReflectionTestUtils;

import com.hospital.appointment.client.PatientServiceClient;
import com.hospital.appointment.dto.AppointmentCreateRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.common.enums.AppointmentStatus;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.common.exception.ValidationException;
import com.hospital.common.kafka.KafkaProducerService;

import io.micrometer.core.instrument.Counter;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked"})
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientServiceClient patientServiceClient;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private Counter appointmentCreationCounter;

    @Mock
    private Counter appointmentCancellationCounter;

    @Mock
    private Counter noShowCounter;

    @Mock
    private Counter schedulingConflictCounter;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private AppointmentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // business rules
        ReflectionTestUtils.setField(appointmentService, "businessHourStart", 8);
        ReflectionTestUtils.setField(appointmentService, "businessHourEnd", 18);
        ReflectionTestUtils.setField(appointmentService, "minAdvanceHours", 1);
        ReflectionTestUtils.setField(appointmentService, "maxDaysInAdvance", 90);
        ReflectionTestUtils.setField(appointmentService, "allowWeekendBooking", false);

        testAppointment = new Appointment(
                1L,
                10L,
                20L,
                null,
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                AppointmentStatus.SCHEDULED,
                "Initial visit",
                null,
                30
        );

        createRequest = AppointmentCreateRequest.builderFallbackInternal()
                .patientId(10L)
                .doctorId(20L)
                .appointmentDateTime(testAppointment.getAppointmentDateTime())
                .durationMinutes(30)
                .reason("Checkup")
                .build();
    // ensure kafka topic name is set on service under test
    ReflectionTestUtils.setField(appointmentService, "appointmentEventsTopic", "appointment-events");
    }

    @Test
    void createAppointment_happyPath_savesPublishesAndIncrements() {
        when(patientServiceClient.validatePatientExists(10L)).thenReturn(true);
        when(patientServiceClient.isPatientActive(10L)).thenReturn(true);
    when(appointmentRepository.findOverlappingAppointments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(appointmentMapper.toEntity(createRequest)).thenReturn(testAppointment);
        Appointment saved = new Appointment(
                99L,
                testAppointment.getPatientId(),
                testAppointment.getDoctorId(),
                testAppointment.getDepartmentId(),
                testAppointment.getAppointmentDateTime(),
                testAppointment.getStatus(),
                testAppointment.getNotes(),
                testAppointment.getReason(),
                testAppointment.getDurationMinutes()
        );
        when(appointmentRepository.save(testAppointment)).thenReturn(saved);
        AppointmentResponse mockResp = new AppointmentResponse();
        mockResp.setId(99L);
        mockResp.setPatientId(10L);
        mockResp.setDoctorId(20L);
        mockResp.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentMapper.toResponse(saved)).thenReturn(mockResp);

        AppointmentResponse resp = appointmentService.createAppointment(createRequest);

        assertNotNull(resp);
        verify(appointmentRepository).save(testAppointment);
    ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(99L)), payloadCaptor.capture());
    assertNotNull(payloadCaptor.getValue());
        verify(appointmentCreationCounter).increment();
    }

    @Test
    void createAppointment_invalidPatient_throwsNotFound() {
        when(patientServiceClient.validatePatientExists(10L)).thenReturn(false);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> appointmentService.createAppointment(createRequest));
        assertNotNull(ex);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_inactivePatient_throwsValidation() {
        when(patientServiceClient.validatePatientExists(10L)).thenReturn(true);
        when(patientServiceClient.isPatientActive(10L)).thenReturn(false);
        ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.createAppointment(createRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("not active"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_conflict_incrementsSchedulingCounter_andThrows() {
        when(patientServiceClient.validatePatientExists(10L)).thenReturn(true);
        when(patientServiceClient.isPatientActive(10L)).thenReturn(true);
    when(appointmentRepository.findOverlappingAppointments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(testAppointment));

        ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.createAppointment(createRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("conflict"));
        verify(schedulingConflictCounter).increment();
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_pastTime_throwsValidation() {
    AppointmentCreateRequest past = createRequest.toBuilder().appointmentDateTime(LocalDateTime.now().minusHours(2)).build();
        ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.createAppointment(past));
        assertTrue(ex.getMessage().toLowerCase().contains("advance"));
    }

    @Test
    void createAppointment_outsideBusinessHours_throwsValidation() {
        AppointmentCreateRequest early = createRequest.toBuilder().appointmentDateTime(LocalDateTime.now().plusDays(1).withHour(7)).build();
        ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.createAppointment(early));
        assertTrue(ex.getMessage().toLowerCase().contains("business hours"));
    }

    @Test
    void createAppointment_onWeekend_throwsWhenNotAllowed() {
        // find next Saturday
        LocalDate saturday = LocalDate.now().with(DayOfWeek.SATURDAY);
        AppointmentCreateRequest wknd = createRequest.toBuilder().appointmentDateTime(saturday.atTime(10,0)).build();
        ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.createAppointment(wknd));
        assertTrue(ex.getMessage().toLowerCase().contains("weekend"));
    }

    @Test
    void createAppointment_invalidDuration_throwsValidation() {
    AppointmentCreateRequest bad = createRequest.toBuilder().durationMinutes(25).build();
        ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.createAppointment(bad));
        assertTrue(ex.getMessage().toLowerCase().contains("duration"));
    }

    @Test
    void cancelAppointment_updatesStatus_andPublishesEvent() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

    AppointmentResponse cancelledResp = new AppointmentResponse();
        cancelledResp.setId(1L);
        cancelledResp.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(cancelledResp);

    AppointmentResponse resp = appointmentService.cancelAppointment(1L);
    assertNotNull(resp);
    assertEquals(1L, resp.getId());
    assertEquals(AppointmentStatus.CANCELLED, resp.getStatus());

    ArgumentCaptor<Object> payloadCaptor2 = ArgumentCaptor.forClass(Object.class);
    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(1L)), payloadCaptor2.capture());
    verify(appointmentCancellationCounter).increment();
    }

    @Test
    void cancelAppointment_alreadyCompleted_throwsValidation() {
        Appointment completed = testAppointment.toBuilder().status(AppointmentStatus.COMPLETED).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(completed));
    ValidationException ex = assertThrows(ValidationException.class, () -> appointmentService.cancelAppointment(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot cancel"));
    }

    @Test
    void confirmAppointment_changesStatus_andPublishes() {
        Appointment scheduled = testAppointment.toBuilder().status(AppointmentStatus.SCHEDULED).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(scheduled));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

    appointmentService.confirmAppointment(1L);

    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(1L)), any());
    }

    @Test
    void checkInAppointment_onlyOnDay_allowsCheckIn() {
        Appointment confirmed = testAppointment.toBuilder().status(AppointmentStatus.CONFIRMED).appointmentDateTime(LocalDateTime.now()).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(confirmed));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

    appointmentService.checkInAppointment(1L);
    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(1L)), any());
    }

    @Test
    void startAndCompleteAppointment_flowAndPublishEvents() {
        Appointment checkedIn = testAppointment.toBuilder().status(AppointmentStatus.CHECKED_IN).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(checkedIn));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

    appointmentService.startAppointment(1L);
    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(1L)), any());

        Appointment inProgress = checkedIn.toBuilder().status(AppointmentStatus.IN_PROGRESS).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(inProgress));
    appointmentService.completeAppointment(1L, "All good");
    verify(kafkaProducerService, atLeast(2)).sendEvent(eq("appointment-events"), anyString(), any());
    }

    @Test
    void markNoShow_marksAndIncrements() {
        Appointment scheduled = testAppointment.toBuilder().status(AppointmentStatus.SCHEDULED).appointmentDateTime(LocalDateTime.now().minusMinutes(30)).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(scheduled));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

    appointmentService.markNoShow(1L);
    verify(noShowCounter).increment();
    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(1L)), any());
    }

    @Test
    void rescheduleAppointment_updatesValues_andPublishes() {
        LocalDateTime newDt = LocalDateTime.now().plusDays(3).withHour(11);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
    when(appointmentRepository.findOverlappingAppointments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        AppointmentResponse resp1 = new AppointmentResponse();
        resp1.setId(1L);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(resp1);

    var resp = appointmentService.rescheduleAppointment(1L, newDt, 45);
    assertNotNull(resp);
    verify(kafkaProducerService).sendEvent(eq("appointment-events"), eq(String.valueOf(1L)), any());
    }

    @Test
    void searchAppointments_withCriteria_returnsPage() {
        Page<Appointment> page = new PageImpl<>(List.of(testAppointment));
        when(appointmentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);
        AppointmentResponse pagedResp = new AppointmentResponse();
        pagedResp.setId(1L);
        when(appointmentMapper.toResponse(testAppointment)).thenReturn(pagedResp);

        var res = appointmentService.searchAppointments(new com.hospital.appointment.dto.AppointmentSearchCriteria(), Pageable.unpaged());
        assertNotNull(res);
    }

    @Test
    void getUpcomingAppointments_cachedAndFiltered() {
        when(appointmentRepository.findByPatientIdAndAppointmentDateTimeAfterAndStatusIn(eq(10L), any(LocalDateTime.class), anyIterable(), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(testAppointment));
    var res = appointmentService.getUpcomingAppointments(10L);
    assertEquals(1, res.size());
    var res2 = appointmentService.getUpcomingAppointments(10L);
    assertEquals(1, res2.size());
    }

    @Test
    void getDoctorSchedule_returnsAppointments() {
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq(20L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(testAppointment));
        var res = appointmentService.getDoctorSchedule(20L, LocalDate.now());
        assertEquals(1, res.size());
    }

    @Test
    void kafkaFailure_doesNotBreak_create() {
        when(patientServiceClient.validatePatientExists(10L)).thenReturn(true);
        when(patientServiceClient.isPatientActive(10L)).thenReturn(true);
    when(appointmentRepository.findOverlappingAppointments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(appointmentMapper.toEntity(any())).thenReturn(testAppointment);
        when(appointmentRepository.save(any())).thenReturn(testAppointment.toBuilder().id(123L).build());
    RuntimeException kafkaEx = new RuntimeException("kafka error");
    doThrow(kafkaEx).when(kafkaProducerService).sendEvent(anyString(), anyString(), any());

        assertDoesNotThrow(() -> appointmentService.createAppointment(createRequest));
    }
}

