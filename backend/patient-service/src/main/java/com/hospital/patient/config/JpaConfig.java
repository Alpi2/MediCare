package com.hospital.patient.config;

import com.hospital.common.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.hospital.patient.repository")
public class JpaConfig {

  @Bean
  public AuditorAware<String> auditorAware() {
    // SecurityUtils.getCurrentUsername() returns Optional<String>
    return () -> SecurityUtils.getCurrentUsername().or(() -> Optional.of("system"));
  }
}
