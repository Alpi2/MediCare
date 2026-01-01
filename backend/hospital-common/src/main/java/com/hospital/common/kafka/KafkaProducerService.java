package com.hospital.common.kafka;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
* Lightweight Kafka producer wrapper that relies on the official Kafka clients.
* This avoids pulling Spring Kafka into the common module. Services that prefer
* Spring integration can create a bean that wraps this class or use Spring Kafka.
*/
public class KafkaProducerService {
  private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

  private final KafkaProducer<String, String> producer;
  private final ObjectMapper mapper;

  public KafkaProducerService(Properties producerProperties, ObjectMapper mapper) {
    this.producer = new KafkaProducer<>(producerProperties);
    this.mapper = Objects.requireNonNull(mapper, "ObjectMapper is required");
  }

  public CompletableFuture<RecordMetadata> sendEvent(String topic, String key, Object event) {
    final String payload;
    try {
      payload = mapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      final CompletableFuture<RecordMetadata> fut = new CompletableFuture<>();
      fut.completeExceptionally(e);
      return fut;
    }

    final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);
    final CompletableFuture<RecordMetadata> future = new CompletableFuture<>();
    producer.send(record, (metadata, exception) -> {
      if (exception != null) {
        log.error("Failed to send event to topic: {} key: {}", topic, key, exception);
        future.completeExceptionally(exception);
      } else {
        log.info("Event sent to topic {} partition {} offset {}",
            metadata.topic(), metadata.partition(), metadata.offset());
        future.complete(metadata);
      }
    });
    return future;
  }

  public RecordMetadata sendEventSync(String topic, String key, Object event, long timeoutMs) throws Exception {
    final CompletableFuture<RecordMetadata> fut = sendEvent(topic, key, event);
    return fut.get(timeoutMs, TimeUnit.MILLISECONDS);
  }

  public void close() {
    try {
      producer.close();
    } catch (Exception e) {
      log.warn("Error closing kafka producer", e);
    }
  }
}
