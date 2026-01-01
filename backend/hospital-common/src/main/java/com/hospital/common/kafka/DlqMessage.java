package com.hospital.common.kafka;

import java.time.LocalDateTime;

import com.hospital.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "dlq_messages")
public class DlqMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "message_key")
    private String key;

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Lob
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers;

    @Column(name = "received_at")
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Column(name = "processed")
    private boolean processed = false;

    @Column(name = "replay_attempts")
    private int replayAttemptCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public int getReplayAttemptCount() { return replayAttemptCount; }
    public void setReplayAttemptCount(int replayAttemptCount) { this.replayAttemptCount = replayAttemptCount; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}