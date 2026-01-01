package com.hospital.appointment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     @Value("${spring.cache.redis.key-prefix:appointment-service::}") String keyPrefix) {
        Objects.requireNonNull(connectionFactory, "connectionFactory must not be null");
        Objects.requireNonNull(keyPrefix, "keyPrefix must not be null");

        Duration defaultTtl = Objects.requireNonNull(Duration.ofMinutes(5), "default TTL must not be null");
        Duration patientAppointmentsTtl = Objects.requireNonNull(Duration.ofMinutes(3), "patientAppointments TTL must not be null");
        Duration doctorScheduleTtl = Objects.requireNonNull(Duration.ofMinutes(2), "doctorSchedule TTL must not be null");
        Duration patientNamesTtl = Objects.requireNonNull(Duration.ofMinutes(30), "patientNames TTL must not be null");

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith(keyPrefix);

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("appointments-page", defaultConfig);
        cacheConfigs.put("appointment-by-id", defaultConfig);
        cacheConfigs.put("patient-appointments", defaultConfig.entryTtl(patientAppointmentsTtl));
        cacheConfigs.put("doctor-schedule", defaultConfig.entryTtl(doctorScheduleTtl));
        cacheConfigs.put("patient-names", defaultConfig.entryTtl(patientNamesTtl));

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setEnableTransactionSupport(true);
        return template;
    }
}
