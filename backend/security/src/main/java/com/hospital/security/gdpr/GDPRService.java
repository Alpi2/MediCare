package com.hospital.security.gdpr;

import java.io.IOException;

public interface GDPRService {
    byte[] exportData(String patientId) throws IOException;
    void eraseData(String patientId);
}
