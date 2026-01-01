package com.hospital.common.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dlq")
public class DlqController {
    private static final Logger log = LoggerFactory.getLogger(DlqController.class);

    private final DlqService dlqService;

    public DlqController(DlqService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping
    public ResponseEntity<Page<DlqMessage>> list(Pageable pageable) {
        Page<DlqMessage> page = dlqService.listDlqMessages(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DlqMessage> get(@PathVariable("id") Long id) {
        DlqMessage m = dlqService.getMessage(id);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }

    @PostMapping("/{id}/replay")
    public ResponseEntity<Void> replay(@PathVariable("id") Long id) {
        DlqMessage m = dlqService.getMessage(id);
        if (m == null) return ResponseEntity.notFound().build();
        // Note: actual replay implementation to re-publish to Kafka belongs to a dedicated service.
        dlqService.incrementReplayAttempt(id, null);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable("id") Long id) {
        DlqMessage m = dlqService.getMessage(id);
        if (m == null) return ResponseEntity.notFound().build();
        dlqService.markProcessed(id);
        return ResponseEntity.noContent().build();
    }
}