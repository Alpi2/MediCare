package com.hospital.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hospital.appointment", "com.hospital.common"})
public class AppointmentServiceSimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceSimpleApplication.class, args);
    }
}