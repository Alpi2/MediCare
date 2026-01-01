package com.hospital.patient.config;

/*
* NOTE: This configuration requires the Springdoc OpenAPI starter
* dependency to be present in the service POM, for example:
*
* <dependency>
*   <groupId>org.springdoc</groupId>
*   <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
*   <version>${openapi.version}</version>
* </dependency>
*
* Ensure the `openapi.version` property matches the springdoc version used    across services.
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
* OpenAPI / Swagger configuration for Patient Service.
*
* Properties to configure in `application.properties` (examples):
*
* springdoc.api-docs.path=/api-docs
* springdoc.swagger-ui.path=/swagger-ui.html
* springdoc.swagger-ui.operationsSorter=method
* springdoc.swagger-ui.tagsSorter=alpha
* springdoc.swagger-ui.try-it-out-enabled=true
*
* Server URLs can be supplied via properties:
* patient.service.staging.url and patient.service.production.url
*/
@Configuration
@OpenAPIDefinition(
info = @Info(
title = "Patient Service API",
version = "1.0.0",
description = "Patient Service providing patient registration, medical records    management, search and statistics",
contact = @Contact(name = "Hospital Dev Team", email = "${CONTACT_EMAIL}", url =    "${documentation.portal.url:https://docs.hospital.example.com}")
),
servers = {
  @Server(url = "http://localhost:8081", description = "Development Server"),
  @Server(url =      "${patient.service.staging.url:http://staging.hospital.example.com}",      description = "Staging Server"),
  @Server(url =      "${patient.service.production.url:https://api.hospital.example.com}",      description = "Production Server")
},
tags = {
  @Tag(name = "Patient Management", description = "CRUD operations for patient      entities"),
  @Tag(name = "Medical Records", description = "Patient history, encounters and      vital signs"),
  @Tag(name = "Statistics", description = "Analytics and reporting endpoints")
},
security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecurityScheme(
name = "bearerAuth",
type = SecuritySchemeType.HTTP,
scheme = "bearer",
bearerFormat = "JWT"
)
public final class OpenApiConfig {

  /**
  * GroupedOpenApi bean for grouping Patient Service APIs and referencing
  * the Springdoc properties for API docs and Swagger UI path customization.
  *
  * Additional UI behaviors (try-it-out, sorters) are configured via
  * `application.properties` using the `springdoc.swagger-ui.*` properties.
  */
  @Bean
  public GroupedOpenApi patientServiceApi(
  @Value("${springdoc.api-docs.path:/api-docs}") String apiDocsPath,
  @Value("${springdoc.swagger-ui.path:/swagger-ui.html}") String swaggerUiPath
  ) {
    return GroupedOpenApi.builder()
    .group("patient-service")
    // match all controller paths in this service; narrow if needed
    .pathsToMatch("/**")
    .build();
  }
}
