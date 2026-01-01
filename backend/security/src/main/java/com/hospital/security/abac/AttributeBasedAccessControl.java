package com.hospital.security.abac;

import com.hospital.security.model.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Attribute-Based Access Control (ABAC) Implementation
 * Provides fine-grained access control based on user, resource, environment, and action attributes
 */
@Component
@Slf4j
public class AttributeBasedAccessControl {

    /**
     * ABAC Policy Decision Point
     * Evaluates access requests based on multiple attributes
     */
    public boolean evaluateAccess(AccessRequest request) {
        try {
            // Get current user context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Build evaluation context
            EvaluationContext context = EvaluationContext.builder()
                .subject(buildSubjectAttributes(userPrincipal))
                .resource(request.getResourceAttributes())
                .action(request.getActionAttributes())
                .environment(buildEnvironmentAttributes())
                .build();

            // Evaluate policies
            return evaluatePolicies(context);
            
        } catch (Exception e) {
            log.error("Error evaluating ABAC access request", e);
            return false;
        }
    }

    /**
     * Build subject (user) attributes
     */
    private Map<String, Object> buildSubjectAttributes(UserPrincipal userPrincipal) {
        Map<String, Object> attributes = new HashMap<>();
        
        attributes.put("userId", userPrincipal.getId().toString());
        attributes.put("username", userPrincipal.getUsername());
        attributes.put("role", userPrincipal.getRole().name());
        attributes.put("email", userPrincipal.getEmail());
        
        // Medical-specific attributes
        if (userPrincipal.getDoctorId() != null) {
            attributes.put("doctorId", userPrincipal.getDoctorId().toString());
        }
        if (userPrincipal.getPatientId() != null) {
            attributes.put("patientId", userPrincipal.getPatientId().toString());
        }
        if (userPrincipal.getDepartmentId() != null) {
            attributes.put("departmentId", userPrincipal.getDepartmentId().toString());
        }
        
        // Add specialization for doctors
        if (userPrincipal.getSpecialization() != null) {
            attributes.put("specialization", userPrincipal.getSpecialization());
        }
        
        return attributes;
    }

    /**
     * Build environment attributes
     */
    private Map<String, Object> buildEnvironmentAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        attributes.put("currentTime", now);
        attributes.put("currentHour", now.getHour());
        attributes.put("dayOfWeek", now.getDayOfWeek().getValue());
        attributes.put("isWeekend", now.getDayOfWeek().getValue() >= 6);
        
        // Business hours (8 AM to 6 PM)
        LocalTime currentTime = now.toLocalTime();
        boolean isBusinessHours = currentTime.isAfter(LocalTime.of(8, 0)) && 
                                 currentTime.isBefore(LocalTime.of(18, 0));
        attributes.put("isBusinessHours", isBusinessHours);
        
        // Emergency hours (outside business hours)
        attributes.put("isEmergencyHours", !isBusinessHours);
        
        return attributes;
    }

    /**
     * Evaluate ABAC policies
     */
    private boolean evaluatePolicies(EvaluationContext context) {
        // Policy 1: Patient data access
        if (isPatientDataAccess(context)) {
            return evaluatePatientDataPolicy(context);
        }
        
        // Policy 2: Medical record access
        if (isMedicalRecordAccess(context)) {
            return evaluateMedicalRecordPolicy(context);
        }
        
        // Policy 3: Prescription access
        if (isPrescriptionAccess(context)) {
            return evaluatePrescriptionPolicy(context);
        }
        
        // Policy 4: Lab result access
        if (isLabResultAccess(context)) {
            return evaluateLabResultPolicy(context);
        }
        
        // Policy 5: Emergency access
        if (isEmergencyAccess(context)) {
            return evaluateEmergencyPolicy(context);
        }
        
        // Policy 6: Administrative access
        if (isAdministrativeAccess(context)) {
            return evaluateAdministrativePolicy(context);
        }
        
        // Policy 7: Time-based access
        if (isTimeRestrictedAccess(context)) {
            return evaluateTimeBasedPolicy(context);
        }
        
        // Default deny
        return false;
    }

    /**
     * Patient data access policy
     */
    private boolean evaluatePatientDataPolicy(EvaluationContext context) {
        String userRole = (String) context.getSubject().get("role");
        String targetPatientId = (String) context.getResource().get("patientId");
        String userPatientId = (String) context.getSubject().get("patientId");
        String userDoctorId = (String) context.getSubject().get("doctorId");
        String userDepartmentId = (String) context.getSubject().get("departmentId");
        String patientDepartmentId = (String) context.getResource().get("departmentId");
        
        // Admin can access all patient data
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // Patient can only access their own data
        if ("PATIENT".equals(userRole)) {
            return userPatientId != null && userPatientId.equals(targetPatientId);
        }
        
        // Doctor can access patients in their department or assigned to them
        if ("DOCTOR".equals(userRole)) {
            // Check if patient is assigned to this doctor
            List<String> assignedPatients = (List<String>) context.getResource().get("assignedDoctors");
            if (assignedPatients != null && userDoctorId != null && assignedPatients.contains(userDoctorId)) {
                return true;
            }
            
            // Check if patient is in the same department
            return userDepartmentId != null && userDepartmentId.equals(patientDepartmentId);
        }
        
        // Nurse can access patients in their department
        if ("NURSE".equals(userRole)) {
            return userDepartmentId != null && userDepartmentId.equals(patientDepartmentId);
        }
        
        return false;
    }

    /**
     * Medical record access policy
     */
    private boolean evaluateMedicalRecordPolicy(EvaluationContext context) {
        String userRole = (String) context.getSubject().get("role");
        String action = (String) context.getAction().get("type");
        String recordDoctorId = (String) context.getResource().get("doctorId");
        String userDoctorId = (String) context.getSubject().get("doctorId");
        
        // Admin can perform all actions
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // Doctor can read all records but only write/modify their own
        if ("DOCTOR".equals(userRole)) {
            if ("READ".equals(action)) {
                return true; // Doctors can read all medical records
            }
            if ("write".equals(action) || "update".equals(action)) {
                return userDoctorId != null && userDoctorId.equals(recordDoctorId);
            }
        }
        
        // Nurse can read medical records
        if ("NURSE".equals(userRole) && "read".equals(action)) {
            return true;
        }
        
        return false;
    }

    /**
     * Prescription access policy
     */
    private boolean evaluatePrescriptionPolicy(EvaluationContext context) {
        String userRole = (String) context.getSubject().get("role");
        String action = (String) context.getAction().get("type");
        String prescribingDoctorId = (String) context.getResource().get("prescribingDoctorId");
        String userDoctorId = (String) context.getSubject().get("doctorId");
        
        // Admin can perform all actions
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // Doctor can prescribe and read prescriptions
        if ("DOCTOR".equals(userRole)) {
            if ("read".equals(action)) {
                return true;
            }
            if ("write".equals(action) || "prescribe".equals(action)) {
                return userDoctorId != null;
            }
            if ("update".equals(action) || "cancel".equals(action)) {
                return userDoctorId != null && userDoctorId.equals(prescribingDoctorId);
            }
        }
        
        // Pharmacist can read and dispense prescriptions
        if ("PHARMACIST".equals(userRole)) {
            return "read".equals(action) || "dispense".equals(action);
        }
        
        return false;
    }

    /**
     * Lab result access policy
     */
    private boolean evaluateLabResultPolicy(EvaluationContext context) {
        String userRole = (String) context.getSubject().get("role");
        String action = (String) context.getAction().get("type");
        
        // Admin can perform all actions
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // Lab technician can read and write lab results
        if ("LAB_TECHNICIAN".equals(userRole)) {
            return "read".equals(action) || "write".equals(action) || "update".equals(action);
        }
        
        // Doctor can read lab results
        if ("DOCTOR".equals(userRole) && "read".equals(action)) {
            return true;
        }
        
        // Nurse can read lab results
        if ("NURSE".equals(userRole) && "read".equals(action)) {
            return true;
        }
        
        return false;
    }

    /**
     * Emergency access policy
     */
    private boolean evaluateEmergencyPolicy(EvaluationContext context) {
        String userRole = (String) context.getSubject().get("role");
        Boolean isEmergency = (Boolean) context.getResource().get("isEmergency");
        Boolean isEmergencyHours = (Boolean) context.getEnvironment().get("isEmergencyHours");
        
        // Emergency access for medical staff during emergency situations
        if (Boolean.TRUE.equals(isEmergency) || Boolean.TRUE.equals(isEmergencyHours)) {
            return "DOCTOR".equals(userRole) || "NURSE".equals(userRole) || "ADMIN".equals(userRole);
        }
        
        return false;
    }

    /**
     * Administrative access policy
     */
    private boolean evaluateAdministrativePolicy(EvaluationContext context) {
        String userRole = (String) context.getSubject().get("role");
        String resourceType = (String) context.getResource().get("type");
        
        // Only admin can access system administration resources
        if ("system".equals(resourceType) || "audit".equals(resourceType)) {
            return "ADMIN".equals(userRole);
        }
        
        // Department management
        if ("department".equals(resourceType)) {
            return "ADMIN".equals(userRole) || "DOCTOR".equals(userRole);
        }
        
        return false;
    }

    /**
     * Time-based access policy
     */
    private boolean evaluateTimeBasedPolicy(EvaluationContext context) {
        Boolean isBusinessHours = (Boolean) context.getEnvironment().get("isBusinessHours");
        String userRole = (String) context.getSubject().get("role");
        String action = (String) context.getAction().get("type");
        
        // Restrict certain actions to business hours
        if ("schedule".equals(action) || "administrative".equals(action)) {
            // Admin and emergency staff can work outside business hours
            if (!Boolean.TRUE.equals(isBusinessHours)) {
                return "ADMIN".equals(userRole) || "DOCTOR".equals(userRole);
            }
        }
        
        return true; // Allow during business hours
    }

    // Helper methods to identify policy types
    private boolean isPatientDataAccess(EvaluationContext context) {
        return "patient".equals(context.getResource().get("type"));
    }

    private boolean isMedicalRecordAccess(EvaluationContext context) {
        return "medical_record".equals(context.getResource().get("type"));
    }

    private boolean isPrescriptionAccess(EvaluationContext context) {
        return "prescription".equals(context.getResource().get("type"));
    }

    private boolean isLabResultAccess(EvaluationContext context) {
        return "lab_result".equals(context.getResource().get("type"));
    }

    private boolean isEmergencyAccess(EvaluationContext context) {
        return Boolean.TRUE.equals(context.getResource().get("isEmergency"));
    }

    private boolean isAdministrativeAccess(EvaluationContext context) {
        String resourceType = (String) context.getResource().get("type");
        return "system".equals(resourceType) || "audit".equals(resourceType) || "department".equals(resourceType);
    }

    private boolean isTimeRestrictedAccess(EvaluationContext context) {
        String action = (String) context.getAction().get("type");
        return "schedule".equals(action) || "administrative".equals(action);
    }
}