package com.hospital.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Small factory for a pre-configured ObjectMapper. This class is intentionally
 * free of Spring annotations so the common module does not introduce
 * auto-configuration. Applications may call {@link #createObjectMapper()} or
 * register the returned instance as a bean in their own configuration.
 */
final class ObjectMapperFactory {

  private ObjectMapperFactory() {
  }

  public static ObjectMapper createObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.findAndRegisterModules();
    return mapper;
  }
}
