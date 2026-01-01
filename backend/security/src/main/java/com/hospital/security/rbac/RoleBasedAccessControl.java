package com.hospital.security.rbac;

import com.hospital.security.model.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Role-Based Access Control (RBAC) Implementation
 * Defines roles, permissions, and access control rules for hospital system
 */
@Component
@Slf4j
public class RoleBasedAccessControl {

    // Define system roles
    public enum Role {
        ADMIN,
        DOCTOR,
        NURSE,
        PATIENT,
        LAB_TECHNICIAN,
        PHARMACIST,
        RECEPTIONIST
    }

    // Define system permissions
    public enum Permission {
        // Patient permissions
        READ_PATIENT,
        WRITE_PATIENT,
        DELETE_PATIENT,
        READ_OWN_PATIENT_DATA,
        
        // Medical record permissions
        READ_MEDICAL_RECORD,
        WRITE_MEDICAL_RECORD,
        DELETE_MEDICAL_RECORD,
        
        // Appointment permissions
        READ_APPOINTMENT,
        WRITE_APPOINTMENT,
        DELETE_APPOINTMENT,
        SCHEDULE_APPOINTMENT,
        
        // Prescription permissions
        READ_PRESCRIPTION,
        WRITE_PRESCRIPTION,
        DELETE_PRESCRIPTION,
        DISPENSE_MEDICATION,
        
        // Lab result permissions
        READ_LAB_RESULT,
        WRITE_LAB_RESULT,
        DELETE_LAB_RESULT,
        
        // Imaging permissions
        READ_IMAGING,
        WRITE_IMAGING,
        DELETE_IMAGING,
        
        // Vital signs permissions
        READ_VITAL_SIGNS,
        WRITE_VITAL_SIGNS,
        
        // AI/Analytics permissions
        READ_AI_PREDICTIONS,
        WRITE_AI_PREDICTIONS,
        READ_ANALYTICS,
        WRITE_ANALYTICS,
        
        // System administration
        MANAGE_USERS,
        MANAGE_SYSTEM,
        VIEW_AUDIT_LOGS,
        MANAGE_EQUIPMENT,
        
        // Department permissions
        MANAGE_DEPARTMENT,
        READ_DEPARTMENT_DATA,
        
        // Emergency permissions
        EMERGENCY_ACCESS
    }

    // Role-Permission mapping
    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // ADMIN - Full system access
        ROLE_PERMISSIONS.put(Role.ADMIN, EnumSet.allOf(Permission.class));

        // DOCTOR - Clinical access with patient care focus
        ROLE_PERMISSIONS.put(Role.DOCTOR, EnumSet.of(
            Permission.READ_PATIENT,
            Permission.WRITE_PATIENT,
            Permission.READ_MEDICAL_RECORD,
            Permission.WRITE_MEDICAL_RECORD,
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT,
            Permission.SCHEDULE_APPOINTMENT,
            Permission.READ_PRESCRIPTION,
            Permission.WRITE_PRESCRIPTION,
            Permission.READ_LAB_RESULT,
            Permission.WRITE_LAB_RESULT,
            Permission.READ_IMAGING,
            Permission.WRITE_IMAGING,
            Permission.READ_VITAL_SIGNS,
            Permission.WRITE_VITAL_SIGNS,
            Permission.READ_AI_PREDICTIONS,
            Permission.WRITE_AI_PREDICTIONS,
            Permission.READ_ANALYTICS,
            Permission.READ_DEPARTMENT_DATA,
            Permission.EMERGENCY_ACCESS
        ));

        // NURSE - Patient care and monitoring
        ROLE_PERMISSIONS.put(Role.NURSE, EnumSet.of(
            Permission.READ_PATIENT,
            Permission.WRITE_PATIENT,
            Permission.READ_MEDICAL_RECORD,
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT,
            Permission.READ_PRESCRIPTION,
            Permission.READ_LAB_RESULT,
            Permission.READ_IMAGING,
            Permission.READ_VITAL_SIGNS,
            Permission.WRITE_VITAL_SIGNS,
            Permission.READ_AI_PREDICTIONS,
            Permission.EMERGENCY_ACCESS
        ));

        // PATIENT - Own data access only
        ROLE_PERMISSIONS.put(Role.PATIENT, EnumSet.of(
            Permission.READ_OWN_PATIENT_DATA,
            Permission.READ_APPOINTMENT,
            Permission.SCHEDULE_APPOINTMENT,
            Permission.READ_PRESCRIPTION,
            Permission.READ_LAB_RESULT,
            Permission.READ_IMAGING,
            Permission.READ_VITAL_SIGNS
        ));

        // LAB_TECHNICIAN - Lab-focused access
        ROLE_PERMISSIONS.put(Role.LAB_TECHNICIAN, EnumSet.of(
            Permission.READ_PATIENT,
            Permission.READ_LAB_RESULT,
            Permission.WRITE_LAB_RESULT,
            Permission.READ_APPOINTMENT,
            Permission.READ_VITAL_SIGNS
        ));

        // PHARMACIST - Medication-focused access
        ROLE_PERMISSIONS.put(Role.PHARMACIST, EnumSet.of(
            Permission.READ_PATIENT,
            Permission.READ_PRESCRIPTION,
            Permission.WRITE_PRESCRIPTION,
            Permission.DISPENSE_MEDICATION,
            Permission.READ_APPOINTMENT,
            Permission.READ_MEDICAL_RECORD
        ));

        // RECEPTIONIST - Administrative access
        ROLE_PERMISSIONS.put(Role.RECEPTIONIST, EnumSet.of(
            Permission.READ_PATIENT,
            Permission.WRITE_PATIENT,
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT,
            Permission.SCHEDULE_APPOINTMENT,
            Permission.DELETE_APPOINTMENT
        ));
    }

    /**
     * Check if current user has specific permission
     */
    public boolean hasPermission(Permission permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return hasPermission(userPrincipal.getRole(), permission);
    }

    /**
     * Check if role has specific permission
     */
    public boolean hasPermission(Role role, Permission permission) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Get all permissions for a role
     */
    public Set<Permission> getPermissions(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    /**
     * Check if current user can access patient data
     */
    public boolean canAccessPatient(String patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Role role = userPrincipal.getRole();

        // Admin can access all patients
        if (role == Role.ADMIN) {
            return true;
        }

        // Patient can only access their own data
        if (role == Role.PATIENT) {
            return userPrincipal.getPatientId() != null && 
                   userPrincipal.getPatientId().toString().equals(patientId);
        }

        // Healthcare providers need appropriate permissions
        return hasPermission(role, Permission.READ_PATIENT);
    }

    /**
     * Check if current user can access medical record
     */
    public boolean canAccessMedicalRecord(String patientId, String doctorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Role role = userPrincipal.getRole();

        // Admin can access all records
        if (role == Role.ADMIN) {
            return true;
        }

        // Patient can access their own records
        if (role == Role.PATIENT) {
            return userPrincipal.getPatientId() != null && 
                   userPrincipal.getPatientId().toString().equals(patientId);
        }

        // Doctor can access records they created or patients assigned to them
        if (role == Role.DOCTOR) {
            return hasPermission(Permission.READ_MEDICAL_RECORD) &&
                   (userPrincipal.getDoctorId() != null && 
                    userPrincipal.getDoctorId().toString().equals(doctorId));
        }

        // Nurses can read medical records
        if (role == Role.NURSE) {
            return hasPermission(Permission.READ_MEDICAL_RECORD);
        }

        return false;
    }

    /**
     * Check if current user can access department data
     */
    public boolean canAccessDepartment(String departmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Role role = userPrincipal.getRole();

        // Admin can access all departments
        if (role == Role.ADMIN) {
            return true;
        }

        // Healthcare providers can access their own department
        if (role == Role.DOCTOR || role == Role.NURSE) {
            return userPrincipal.getDepartmentId() != null && 
                   userPrincipal.getDepartmentId().toString().equals(departmentId);
        }

        return false;
    }

    /**
     * Check emergency access override
     */
    public boolean hasEmergencyAccess() {
        return hasPermission(Permission.EMERGENCY_ACCESS);
    }

    /**
     * Get current user's role
     */
    public Role getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getRole();
    }

    /**
     * Get current user's ID
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId().toString();
    }

    /**
     * Check if user can perform action on resource
     */
    public boolean canPerformAction(String action, String resource, Map<String, Object> context) {
        Role role = getCurrentUserRole();
        if (role == null) {
            return false;
        }

        // Map action-resource combinations to permissions
        String permissionKey = action.toUpperCase() + "_" + resource.toUpperCase();
        
        try {
            Permission permission = Permission.valueOf(permissionKey);
            return hasPermission(role, permission);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown permission: {}", permissionKey);
            return false;
        }
    }

    /**
     * Log access attempt for audit purposes
     */
    public void logAccessAttempt(String resource, String action, boolean granted) {
        String userId = getCurrentUserId();
        Role role = getCurrentUserRole();
        
        log.info("Access attempt - User: {}, Role: {}, Resource: {}, Action: {}, Granted: {}", 
                userId, role, resource, action, granted);
    }
}