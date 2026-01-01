package com.hospital.security.abac;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * ABAC Access Request
 * Represents a request for access to a resource
 */
@Data
@Builder
public class AccessRequest {
    private Map<String, Object> subjectAttributes;
    private Map<String, Object> resourceAttributes;
    private Map<String, Object> actionAttributes;
    private Map<String, Object> environmentAttributes;

    // No-arg constructor for manual builder fallback and deserialization
    public AccessRequest() {}

    // Fallback builder for IDEs without Lombok
    public static AccessRequestBuilder builderFallbackInternal() { return new AccessRequestBuilder(); }
    // Delegate matching Lombok API
    public static AccessRequestBuilder builder() { return builderFallbackInternal(); }

    public static class AccessRequestBuilder {
        private Map<String, Object> subjectAttributes;
        private Map<String, Object> resourceAttributes;
        private Map<String, Object> actionAttributes;
        private Map<String, Object> environmentAttributes;

        public AccessRequestBuilder subjectAttributes(Map<String, Object> m) { this.subjectAttributes = m; return this; }
        public AccessRequestBuilder resourceAttributes(Map<String, Object> m) { this.resourceAttributes = m; return this; }
        public AccessRequestBuilder actionAttributes(Map<String, Object> m) { this.actionAttributes = m; return this; }
        public AccessRequestBuilder environmentAttributes(Map<String, Object> m) { this.environmentAttributes = m; return this; }

        public AccessRequest build() {
            AccessRequest r = new AccessRequest();
            r.subjectAttributes = this.subjectAttributes;
            r.resourceAttributes = this.resourceAttributes;
            r.actionAttributes = this.actionAttributes;
            r.environmentAttributes = this.environmentAttributes;
            return r;
        }
    }

    public static AccessRequestBuilder forResource(String resourceType, String resourceId) {
        return AccessRequest.builderFallbackInternal()
                .resourceAttributes(Map.of(
                    "type", resourceType,
                    "id", resourceId
                ));
    }

    public static AccessRequestBuilder forPatient(String patientId) {
        return AccessRequest.builderFallbackInternal()
                .resourceAttributes(Map.of(
                    "type", "patient",
                    "patientId", patientId
                ));
    }

    public static AccessRequestBuilder forMedicalRecord(String recordId, String patientId, String doctorId) {
        return AccessRequest.builderFallbackInternal()
                .resourceAttributes(Map.of(
                    "type", "medical_record",
                    "id", recordId,
                    "patientId", patientId,
                    "doctorId", doctorId
                ));
    }

    public static AccessRequestBuilder forPrescription(String prescriptionId, String patientId, String doctorId) {
        return AccessRequest.builderFallbackInternal()
                .resourceAttributes(Map.of(
                    "type", "prescription",
                    "id", prescriptionId,
                    "patientId", patientId,
                    "prescribingDoctorId", doctorId
                ));
    }

    public AccessRequest withAction(String actionType) {
        this.actionAttributes = Map.of("type", actionType);
        return this;
    }

    public AccessRequest withEmergency(boolean isEmergency) {
        if (this.resourceAttributes == null) {
            this.resourceAttributes = new java.util.HashMap<>();
        }
        this.resourceAttributes.put("isEmergency", isEmergency);
        return this;
    }
}