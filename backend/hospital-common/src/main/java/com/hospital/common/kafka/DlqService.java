package com.hospital.common.kafka;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DlqService {
    DlqMessage saveDlqMessage(String topic, String key, String payload, String headers);
    Page<DlqMessage> listDlqMessages(Pageable pageable);
    DlqMessage getMessage(Long id);
    void markProcessed(Long id);
    void incrementReplayAttempt(Long id, String lastError);
}
