package com.hospital.appointment.config;

import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.common.enums.AppointmentStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final AppointmentRepository appointmentRepository;

    // Explicit constructor replacing Lombok's @RequiredArgsConstructor
    public MetricsConfig(MeterRegistry meterRegistry, AppointmentRepository appointmentRepository) {
        this.meterRegistry = meterRegistry;
        this.appointmentRepository = appointmentRepository;
    }

    @Bean
    public Gauge appointmentCountGauge() {
        return Gauge.builder("hospital.appointments.total", appointmentRepository, repo -> repo.count())
                .description("Total number of appointments")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    @Bean
    public List<Gauge> appointmentsByStatusGauges() {
        List<Gauge> gauges = new ArrayList<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            Gauge g = Gauge.builder("hospital.appointments.by_status", appointmentRepository, repo -> {
                Long c = repo.countByStatus(status);
                return c == null ? 0.0 : c.doubleValue();
            })
                    .description("Appointments by status")
                    .tag("service", "appointment-service")
                    .tag("status", status.name())
                    .register(meterRegistry);
            gauges.add(g);
        }
        return gauges;
    }

    @Bean
    public Counter appointmentCreationCounter() {
        return Counter.builder("hospital.appointments.created")
                .description("Total appointment creations")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter appointmentCancellationCounter() {
        return Counter.builder("hospital.appointments.cancelled")
                .description("Total appointment cancellations")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter noShowCounter() {
        return Counter.builder("hospital.appointments.no_shows")
                .description("Total appointment no-shows")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter schedulingConflictCounter() {
        return Counter.builder("hospital.appointments.conflicts")
                .description("Scheduling conflict occurrences")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    @Bean
    public DistributionSummary appointmentDurationSummary() {
        return DistributionSummary.builder("hospital.appointments.duration")
                .baseUnit("minutes")
                .description("Appointment duration distribution in minutes")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer appointmentProcessingTimer() {
        return Timer.builder("hospital.appointments.processing.duration")
                .description("Appointment processing operation duration")
                .tag("service", "appointment-service")
                .register(meterRegistry);
    }

    // Optional periodic refresh to ensure gauges read fresh values (can warm caches)
    @Scheduled(fixedRate = 60000)
    public void refreshAppointmentMetrics() {
        try {
            appointmentRepository.count();
            for (AppointmentStatus status : AppointmentStatus.values()) {
                appointmentRepository.countByStatus(status);
            }
        } catch (Exception ignored) {
            // Avoid throwing from scheduler; logging can be added if desired
        }
    }
}
