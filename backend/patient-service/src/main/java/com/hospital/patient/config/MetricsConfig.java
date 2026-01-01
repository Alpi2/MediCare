package com.hospital.patient.config;

import com.hospital.patient.repository.PatientRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public final class MetricsConfig {
  /** Meter registry. */

  private final MeterRegistry meterRegistry;
  /** Patient repository. */
  private final PatientRepository patientRepository;

  public MetricsConfig(final MeterRegistry meterRegistry, final PatientRepository patientRepository) {
    this.meterRegistry = meterRegistry;
    this.patientRepository = patientRepository;
  }
  /**
   * Patient count gauge.
   * @return Result.
   */

  @Bean
  public Gauge patientCountGauge() {
    return Gauge.builder("hospital.patients.total", patientRepository, repo ->        repo.count())
    .description("Total number of patients in system")
    .tag("service", "patient-service")
    .register(meterRegistry);
  }
  /**
   * Active patient count gauge.
   * @return Result.
   */

  @Bean
  public Gauge activePatientCountGauge() {
    return Gauge.builder("hospital.patients.active", patientRepository, repo ->        repo.countActivePatients())
    .description("Number of active patients")
    .tag("service", "patient-service")
    .register(meterRegistry);
  }
  /**
   * Patient registration counter.
   * @return Result.
   */

  @Bean
  public Counter patientRegistrationCounter() {
    return Counter.builder("hospital.patients.registrations")
    .description("Total patient registrations")
    .tag("service", "patient-service")
    .register(meterRegistry);
  }
  /**
   * Patient search timer.
   * @return Result.
   */

  @Bean
  public Timer patientSearchTimer() {
    return Timer.builder("hospital.patients.search.duration")
    .description("Patient search operation duration")
    .tag("service", "patient-service")
    .register(meterRegistry);
  }

  // Optional scheduled refresh - triggers periodic DB reads so gauge suppliers      observe fresh values
  /**
   * Refresh metrics.
   */
  @Scheduled(fixedRate = 60000)
  public void refreshMetrics() {
    try {
      // invoke repository methods to encourage any caching layers to refresh
      patientRepository.count();
      patientRepository.countActivePatients();
    } catch (Exception e) {
      // avoid crashing scheduler on transient DB issues
      // log is intentionally avoided here to keep this class side-effect free;          adjust if needed
    }
  }
}
