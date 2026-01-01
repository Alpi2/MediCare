package com.hospital.security.gdpr;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gdpr")
public class GDPRController {

    private static final Logger log = LoggerFactory.getLogger(GDPRController.class);

    private final GDPRService gdprService;

    public GDPRController(GDPRService gdprService) {
        this.gdprService = gdprService;
    }

    @GetMapping("/export/{patientId}")
    public ResponseEntity<ByteArrayResource> exportData(@PathVariable("patientId") String patientId) {
        try {
            byte[] payload = gdprService.exportData(patientId);
            ByteArrayResource resource = new ByteArrayResource(payload);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patient-" + patientId + "-export.json")
                    .contentLength(payload.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            log.error("Failed to export GDPR data for {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            log.error("Unexpected error exporting GDPR data for {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/erase/{patientId}")
    public ResponseEntity<Void> eraseData(@PathVariable("patientId") String patientId) {
        try {
            gdprService.eraseData(patientId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Failed to erase GDPR data for {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
