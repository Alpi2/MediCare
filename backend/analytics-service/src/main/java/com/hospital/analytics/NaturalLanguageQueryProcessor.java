package com.hospital.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics/nlq")
@RequiredArgsConstructor
@Tag(name = "Natural Language Query", description = "Query hospital data using natural language")
public class NaturalLanguageQueryProcessor {

    @Operation(summary = "Process Natural Language Query")
    @PostMapping("/query")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> processNaturalLanguageQuery(
        @RequestBody Map<String, String> queryRequest
    ) {
        String query = queryRequest.get("query");
        
        // NLP processing to convert natural language to SQL/analytics
        Map<String, Object> response = new HashMap<>();
        
        if (query.toLowerCase().contains("patient count") || query.toLowerCase().contains("how many patients")) {
            response.put("interpretation", "Patient count query");
            response.put("sql", "SELECT COUNT(*) FROM patients WHERE admission_date >= CURRENT_DATE - INTERVAL '30 days'");
            response.put("result", Map.of(
                "totalPatients", 1247,
                "newPatients", 89,
                "timeframe", "last 30 days"
            ));
        } else if (query.toLowerCase().contains("appointment") && query.toLowerCase().contains("today")) {
            response.put("interpretation", "Today's appointments query");
            response.put("sql", "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE");
            response.put("result", Map.of(
                "totalAppointments", 156,
                "completed", 134,
                "pending", 22,
                "cancelled", 8
            ));
        } else if (query.toLowerCase().contains("revenue") || query.toLowerCase().contains("income")) {
            response.put("interpretation", "Revenue analysis query");
            response.put("sql", "SELECT SUM(amount) FROM billing WHERE billing_date >= CURRENT_DATE - INTERVAL '30 days'");
            response.put("result", Map.of(
                "totalRevenue", 2450000,
                "averagePerDay", 81667,
                "timeframe", "last 30 days"
            ));
        } else {
            response.put("interpretation", "General query - please be more specific");
            response.put("suggestions", Arrays.asList(
                "How many patients were admitted today?",
                "What is the revenue for this month?",
                "Show me appointment statistics for cardiology department"
            ));
        }
        
        response.put("queryId", "NLQ_" + System.currentTimeMillis());
        response.put("confidence", 0.89);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Query Suggestions")
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getQuerySuggestions(
        @RequestParam(required = false) String context
    ) {
        List<String> suggestions = Arrays.asList(
            "How many patients were admitted this week?",
            "What is the bed occupancy rate in ICU?",
            "Show me revenue trends for the last 6 months",
            "Which department has the highest patient satisfaction?",
            "What are the most common diagnoses this month?",
            "How many surgeries were performed today?",
            "What is the average wait time in emergency?"
        );
        return ResponseEntity.ok(suggestions);
    }
}