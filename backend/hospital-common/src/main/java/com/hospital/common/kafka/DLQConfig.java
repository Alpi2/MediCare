package com.hospital.common.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class DLQConfig {

    // Topic names can be externalized to application.properties / env
    public static final String APPOINTMENT_EVENTS_TOPIC = "appointment-events";
    public static final String APPOINTMENT_EVENTS_DLQ = "appointment-events-dlq";

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(org.springframework.kafka.core.ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
