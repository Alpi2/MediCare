package com.hospital.security.audit;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    public void log(AuditEvent event) {
        // Minimal implementation: log to stdout and later wire to persistent store (Kafka/DB)
        log.info("AUDIT user={} action={} ip={} at={} details={}",
                event.getUser(), event.getAction(), event.getIp(), event.getTimestamp(), event.getDetails());
        // TODO: persist to audit store or publish to Kafka topic for immutability
    }
}
