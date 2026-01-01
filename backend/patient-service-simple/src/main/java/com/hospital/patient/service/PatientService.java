package com.hospital.patient.service;

import com.hospital.patient.entity.Patient;
import com.hospital.common.kafka.KafkaProducerService;
import com.hospital.common.event.PatientEvent;
import org.springframework.beans.factory.annotation.Value;
import com.hospital.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    private final KafkaProducerService kafkaProducerService;

    @Value("${kafka.topics.patient-events}")
    private String patientEventsTopic;

    // Explicit constructor since @RequiredArgsConstructor is not working
    public PatientService(PatientRepository patientRepository, KafkaProducerService kafkaProducerService) {
        this.patientRepository = patientRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    public Patient createPatient(Patient patient) {
        Patient saved = patientRepository.save(patient);
        // Publish patient registered event
        PatientEvent event = PatientEvent.registered(
                saved.getId(),
                saved.getMedicalRecordNumber(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail()
        );
        kafkaProducerService.sendEvent(patientEventsTopic, saved.getId().toString(), event);
        return saved;
    }

    public Patient updatePatient(Long id, Patient patientDetails) {
        Optional<Patient> optionalPatient = patientRepository.findById(id);
        if (optionalPatient.isPresent()) {
            Patient patient = optionalPatient.get();
            patient.setFirstName(patientDetails.getFirstName());
            patient.setLastName(patientDetails.getLastName());
            patient.setEmail(patientDetails.getEmail());
            patient.setPhone(patientDetails.getPhone());
            patient.setDateOfBirth(patientDetails.getDateOfBirth());
            patient.setAddress(patientDetails.getAddress());
            patient.setMedicalRecordNumber(patientDetails.getMedicalRecordNumber());
            Patient saved = patientRepository.save(patient);
            // Publish patient updated event
            PatientEvent event = PatientEvent.updated(
                    saved.getId(),
                    saved.getMedicalRecordNumber()
            );
            kafkaProducerService.sendEvent(patientEventsTopic, saved.getId().toString(), event);
            return saved;
        }
        return null;
    }

    public boolean deletePatient(Long id) {
        if (patientRepository.existsById(id)) {
            Patient patient = patientRepository.findById(id).get();
            patientRepository.deleteById(id);
            // Publish patient deleted event
            PatientEvent event = PatientEvent.deleted(
                    patient.getId(),
                    patient.getMedicalRecordNumber()
            );
            kafkaProducerService.sendEvent(patientEventsTopic, patient.getId().toString(), event);
            return true;
        }
        return false;
    }
}