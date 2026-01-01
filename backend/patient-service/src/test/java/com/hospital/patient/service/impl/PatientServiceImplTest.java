package com.hospital.patient.service.impl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.common.kafka.KafkaProducerService;
import com.hospital.patient.domain.Patient;
import com.hospital.patient.dto.PatientCreateRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.mapper.PatientMapper;
import com.hospital.patient.repository.PatientRepository;

class PatientServiceImplTest {

    private PatientRepository patientRepository;
    private PatientMapper patientMapper;
    private KafkaProducerService kafkaProducerService;
    private PatientServiceImpl service;

    @BeforeEach
    void setup() {
        patientRepository = mock(PatientRepository.class);
        patientMapper = mock(PatientMapper.class);
        kafkaProducerService = mock(KafkaProducerService.class);
        // provide no-op counters/timers
        io.micrometer.core.instrument.Counter counter = mock(io.micrometer.core.instrument.Counter.class);
        io.micrometer.core.instrument.Timer timer = mock(io.micrometer.core.instrument.Timer.class);

        service = new PatientServiceImpl(patientRepository, patientMapper, kafkaProducerService, counter, timer);
    }

    @Test
    void createPatient_success_savesAndPublishesEvent() {
        PatientCreateRequest req = PatientCreateRequest.builderFallbackInternal()
            .firstName("John")
            .lastName("Doe")
            .build();

        Patient entity = new Patient();

        PatientResponse resp = mock(PatientResponse.class);

        when(patientMapper.toEntity(any())).thenReturn(entity);
        when(patientRepository.save(any())).thenReturn(entity);
        when(patientMapper.toResponse(entity)).thenReturn(resp);

        PatientResponse result = service.createPatient(req);

        assertThat(result).isNotNull();
        verify(patientRepository, times(1)).save(entity);
        verify(kafkaProducerService, atLeastOnce()).sendEvent(anyString(), anyString(), any());
    }

    @Test
    void getPatientById_returnsEmpty_whenNotFound() {
        when(patientRepository.findById(42L)).thenReturn(Optional.empty());
        Optional<PatientResponse> r = service.getPatientById(42L);
        assertThat(r).isEmpty();
    }
}
