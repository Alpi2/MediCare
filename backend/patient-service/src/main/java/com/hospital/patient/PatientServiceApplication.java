package com.hospital.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.hospital.patient", "com.hospital.common"})
public final class PatientServiceApplication {
  /**
   * Main.
   * @param args Args.
   */

  public static void main(String[] args) {
    SpringApplication.run(PatientServiceApplication.class, args);
  }
}
