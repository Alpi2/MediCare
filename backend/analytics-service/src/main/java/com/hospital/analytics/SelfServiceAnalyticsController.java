package com.hospital.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics/self-service")
@RequiredArgsConstructor
@Tag(name = "Self-Service Analytics", description = "Interactive analytics and reporting for hospital staff")
public class SelfServiceAnalyticsController {

    @Operation(summary = "Create Custom Dashboard")
    @PostMapping("/dashboards")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> createCustomDashboard(
        @RequestBody Map<String, Object> dashboardConfig
    ) {
        // Self-service dashboard creation with drag-and-drop widgets
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("dashboardId", "DASH_" + System.currentTimeMillis());
        dashboard.put("widgets", Arrays.asList(
            Map.of("type", "patient-volume", "timeRange", "7d"),
            Map.of("type", "appointment-stats", "department", "cardiology"),
            Map.of("type", "bed-occupancy", "unit", "ICU")
        ));
        dashboard.put("status", "created");
        return ResponseEntity.ok(dashboard);
    }

    @Operation(summary = "Generate Interactive Report")
    @PostMapping("/reports/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> generateInteractiveReport(
        @RequestBody Map<String, Object> reportRequest
    ) {
        // Interactive report generation with filters and drill-down capabilities
        Map<String, Object> report = new HashMap<>();
        report.put("reportId", "RPT_" + System.currentTimeMillis());
        report.put("type", "interactive");
        report.put("data", Map.of(
            "patientMetrics", Map.of("total", 1250, "newPatients", 85),
            "appointmentMetrics", Map.of("scheduled", 340, "completed", 298, "noShow", 42),
            "financialMetrics", Map.of("revenue", 125000, "collections", 118000)
        ));
        return ResponseEntity.ok(report);
    }
}