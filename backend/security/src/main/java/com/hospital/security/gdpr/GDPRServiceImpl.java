package com.hospital.security.gdpr;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GDPRServiceImpl implements GDPRService {
    private static final Logger log = LoggerFactory.getLogger(GDPRServiceImpl.class);

    @Override
    public byte[] exportData(String patientId) throws IOException {
        // TODO: orchestrate calls to other services to aggregate patient data
        String payload = "{ \"patientId\": \"" + patientId + "\" }";
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void eraseData(String patientId) {
        // TODO: orchestrate deletion across services and persist audit
        log.info("Erase requested for patientId={}", patientId);
        // no-op for now
    }
}
