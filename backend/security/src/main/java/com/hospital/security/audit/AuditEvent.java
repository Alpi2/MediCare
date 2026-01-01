package com.hospital.security.audit;

import java.time.Instant;

public class AuditEvent {
    private String user;
    private String action;
    private Instant timestamp;
    private String ip;
    private String details;

    public AuditEvent(String user, String action, Instant timestamp, String ip, String details) {
        this.user = user;
        this.action = action;
        this.timestamp = timestamp;
        this.ip = ip;
        this.details = details;
    }

    public String getUser() { return user; }
    public String getAction() { return action; }
    public Instant getTimestamp() { return timestamp; }
    public String getIp() { return ip; }
    public String getDetails() { return details; }
}
