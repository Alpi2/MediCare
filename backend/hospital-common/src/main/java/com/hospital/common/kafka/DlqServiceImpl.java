package com.hospital.common.kafka;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DlqServiceImpl implements DlqService {
    private static final Logger log = LoggerFactory.getLogger(DlqServiceImpl.class);

    private final DlqMessageRepository repository;

    public DlqServiceImpl(DlqMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public DlqMessage saveDlqMessage(String topic, String key, String payload, String headers) {
        DlqMessage m = new DlqMessage();
        m.setTopic(topic);
        m.setKey(key);
        m.setPayload(payload);
        m.setHeaders(headers);
        m.setReceivedAt(LocalDateTime.now());
        DlqMessage saved = repository.save(m);
        log.info("Persisted DLQ message id={} topic={}", saved.getId(), topic);
        return saved;
    }

    @Override
    public Page<DlqMessage> listDlqMessages(Pageable pageable) {
        log.debug("Listing DLQ messages page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Override
    public DlqMessage getMessage(Long id) {
        Optional<DlqMessage> o = repository.findById(id);
        if (o.isPresent()) {
            log.debug("Retrieved DLQ message id={}", id);
            return o.get();
        }
        log.debug("No DLQ message found with id={}", id);
        return null;
    }

    @Override
    public void markProcessed(Long id) {
        repository.findById(id).ifPresent(m -> {
            m.setProcessed(true);
            repository.save(m);
            log.info("Marked DLQ message id={} as processed", id);
        });
    }

    @Override
    public void incrementReplayAttempt(Long id, String lastError) {
        repository.findById(id).ifPresent(m -> {
            m.setReplayAttemptCount(m.getReplayAttemptCount() + 1);
            m.setLastError(lastError);
            repository.save(m);
            log.info("Incremented replay attempts for DLQ message id={} to {}", id, m.getReplayAttemptCount());
        });
    }
}
