package com.hospital.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;

/**
 * OpenAPI / Swagger configuration for Appointment Service.
 *
 * Properties (examples) to place in `application.properties`:
 * springdoc.api-docs.path=/api-docs
 * springdoc.swagger-ui.path=/swagger-ui.html
 * appointment.service.staging.url=http://staging.appointment.example.com
 * appointment.service.production.url=https://api.appointment.example.com
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Appointment Service API",
                version = "1.0.0",
                description = "Appointment Service offering intelligent scheduling, conflict detection, workflow management (SCHEDULED → CONFIRMED → CHECKED_IN → STARTED → COMPLETED/NO_SHOW), and patient validation via Patient Service integration.",
                contact = @Contact(name = "Hospital Dev Team", email = "${CONTACT_EMAIL}", url = "${documentation.portal.url:https://docs.hospital.example.com}")
        ),
        servers = {
                @Server(url = "http://localhost:8082", description = "Development Server"),
                @Server(url = "${appointment.service.staging.url:http://staging.appointment.example.com}", description = "Staging Server"),
                @Server(url = "${appointment.service.production.url:https://api.appointment.example.com}", description = "Production Server")
        },
        tags = {
                @Tag(name = "Appointment Management", description = "CRUD operations for appointments"),
                @Tag(name = "Scheduling", description = "Availability checks and booking operations"),
                @Tag(name = "Workflow Operations", description = "Status transitions: confirm, check-in, start, complete, cancel, reschedule, no-show"),
                @Tag(name = "Search & Filters", description = "Advanced search and filtering for appointments")
        },
        security = {@SecurityRequirement(name = "bearerAuth")}    
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
        public GroupedOpenApi appointmentServiceApi(@Value("${springdoc.api-docs.path:/api-docs}") String apiDocsPath,
                                                                                           @Value("${springdoc.swagger-ui.path:/swagger-ui.html}") String swaggerUiPath) {
        return GroupedOpenApi.builder()
                .group("appointment-service")
                .pathsToMatch("/api/v1/appointments/**")
                .build();
    }

    /**
     * Register common schemas and reusable responses (ErrorResponse, ValidationException)
     * and attach example payloads for typical appointment requests/responses.
     */
    @Bean
        public OpenApiCustomizer appointmentOpenApiCustomiser() {
                return openApi -> {
            Components comps = openApi.getComponents();
            if (comps == null) {
                comps = new Components();
                openApi.setComponents(comps);
            }

            // ErrorResponse schema
            ObjectSchema error = new ObjectSchema();
            error.addProperty("timestamp", new StringSchema().example("2024-01-02T15:04:05Z"));
            error.addProperty("status", new IntegerSchema().example(400));
            error.addProperty("error", new StringSchema().example("Bad Request"));
            error.addProperty("message", new StringSchema().example("Validation failed: startTime must be before endTime"));
            error.addProperty("path", new StringSchema().example("/api/v1/appointments"));
            comps.addSchemas("ErrorResponse", error);

            // ValidationException schema
            ObjectSchema validation = new ObjectSchema();
            validation.addProperty("errors", new ObjectSchema().example("{\"startTime\":\"must be before endTime\"}"));
            validation.addProperty("message", new StringSchema().example("Validation failed"));
            comps.addSchemas("ValidationException", validation);

            // Common responses
            ApiResponse badRequest = new ApiResponse()
                    .description("Invalid input")
                    .content(new Content().addMediaType("application/json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

            ApiResponse validationResp = new ApiResponse()
                    .description("Validation error")
                    .content(new Content().addMediaType("application/json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ValidationException"))));

            ApiResponse unauthorized = new ApiResponse().description("Unauthorized - missing or invalid JWT");
            ApiResponse internalError = new ApiResponse()
                    .description("Internal server error")
                    .content(new Content().addMediaType("application/json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

            comps.addResponses("BadRequest", badRequest);
            comps.addResponses("ValidationError", validationResp);
            comps.addResponses("Unauthorized", unauthorized);
            comps.addResponses("InternalError", internalError);

            // Example request payload schema for booking
            ObjectSchema booking = new ObjectSchema();
            booking.addProperty("patientId", new IntegerSchema().example(123));
            booking.addProperty("mrn", new StringSchema().example("MRN-2024-001234"));
            booking.addProperty("startTime", new StringSchema().example("2025-02-20T09:00:00Z"));
            booking.addProperty("endTime", new StringSchema().example("2025-02-20T09:30:00Z"));
            booking.addProperty("providerId", new IntegerSchema().example(45));
            booking.addProperty("locationId", new IntegerSchema().example(3));
            booking.addProperty("reason", new StringSchema().example("Routine follow-up"));
            comps.addSchemas("AppointmentCreateRequest", booking);
        };
    }
}
