package com.hospital.patient.controller;

import com.hospital.patient.dto.*;
import com.hospital.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.hospital.patient.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
@Slf4j
@Validated
@Tag(name = "Patient Management", description = "APIs for managing patient records")
@SecurityRequirement(name = "bearerAuth")
public final class PatientController {
  private final PatientService patientService;

  /**
  * Explicit constructor to ensure IDEs and tools that do not run Lombok annotation
  * processing still recognize the injected dependency. Keeps behavior equivalent
  * to Lombok's {@code @RequiredArgsConstructor} while avoiding constructor-missing
  * diagnostics in some development environments.
  */
  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  @PostMapping
  @Operation(summary = "Register a new patient in the system",
  description = "Register a new patient in the system with complete demographic information, medical history, and insurance details. Validates SSN uniqueness and generates MRN automatically.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Patient created successfully",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "409", description = "Conflict - duplicate SSN or MRN",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Create patient.
   * @return Result.
   */
  })
  public ResponseEntity<PatientResponse> createPatient(
  @RequestBody(description = "Patient creation payload", required = true,
  content = @Content(mediaType = "application/json",
  schema = @Schema(implementation = PatientCreateRequest.class),
  examples = @ExampleObject(value = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"ssn\":\"123-45-6789\",\"dateOfBirth\":\"1985-04-12\",\"insurance\":{\"provider\":\"Acme Health\",\"policyNumber\":\"POL123456\"}}")))
  @Valid @org.springframework.web.bind.annotation.RequestBody PatientCreateRequest request) {
    PatientResponse resp = patientService.createPatient(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Retrieve patient by ID",
  description = "Fetch a patient's full record using the internal numeric identifier.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Patient returned",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "404", description = "Patient not found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Get patient by id.
   * @return Result.
   */
  })
  public ResponseEntity<PatientResponse> getPatientById(
  @Parameter(description = "Patient unique identifier", example = "123", required = true)
  @PathVariable Long id) {
    return patientService.getPatientById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/mrn/{mrn}")
  @Operation(summary = "Retrieve patient by MRN",
  description = "Fetch a patient's record using the Medical Record Number (MRN).")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Patient returned",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "404", description = "Patient not found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Get by mrn.
   * @return Result.
   */
  })
  public ResponseEntity<PatientResponse> getByMrn(
  @Parameter(description = "Medical Record Number in format MRN-YYYY-NNNNNN", example = "MRN-2024-001234", required = true)
  @PathVariable String mrn) {
    return patientService.getPatientByMRN(mrn).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @Operation(summary = "List patients with pagination",
  description = "Returns a paginated list of patients. Supports sorting and paging parameters.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Paged list returned",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "500", description = "Internal server error",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Get all patients.
   * @return Result.
   */
  })
  public ResponseEntity<Page<PatientResponse>> getAllPatients(
  @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
  @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
  @Parameter(description = "Field to sort by", example = "lastName") @RequestParam(defaultValue = "id") String sortBy,
  @Parameter(description = "Sort direction: ASC or DESC", example = "ASC") @RequestParam(defaultValue = "ASC") String sortDir) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
    Page<PatientResponse> p = patientService.getAllPatients(pageable);
    return ResponseEntity.ok(p);
  }

  @GetMapping("/active")
  @Operation(summary = "List active patients",
  description = "Returns active (non-deactivated) patients with pagination support.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Paged list returned",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
  /**
   * Get active patients.
   * @param @Parameter @ parameter.
   * @return Result.
   */
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT")
  })
  public ResponseEntity<Page<PatientResponse>> getActivePatients(@Parameter(description = "Pagination information") final Pageable pageable) {
    return ResponseEntity.ok(patientService.getActivePatients(pageable));
  }

  @PostMapping("/search")
  @Operation(summary = "Search patients by criteria",
  description = "Search patients using advanced search criteria (demographics, conditions, insurance, etc.).")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search results returned",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "400", description = "Invalid search criteria",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Search patients.
   * @return Result.
   */
  })
  public ResponseEntity<Page<PatientResponse>> searchPatients(
  @RequestBody(description = "Search criteria payload", required = true,
  content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientSearchCriteria.class),
  examples = @ExampleObject(value = "{\"name\":\"Jane\",\"condition\":\"diabetes\"}"))) @org.springframework.web.bind.annotation.RequestBody PatientSearchCriteria criteria,
  @Parameter(description = "Pagination information") Pageable pageable) {
    return ResponseEntity.ok(patientService.searchPatients(criteria, pageable));
  }

  @GetMapping("/search/name")
  @Operation(summary = "Search patients by name",
  description = "Search patients by full or partial name match.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search results returned",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
  /**
   * Search by name.
   * @return Result.
   */
  })
  public ResponseEntity<Page<PatientResponse>> searchByName(
  @Parameter(description = "Name query string", example = "Doe") @RequestParam String query,
  @Parameter(description = "Pagination information") Pageable pageable) {
    return ResponseEntity.ok(patientService.searchPatientsByName(query, pageable));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update patient record",
  description = "Update an existing patient's demographic, medical or insurance information. Validates input and prevents duplicate SSN/MRN conflicts.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Patient updated successfully",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "404", description = "Patient not found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "Conflict - duplicate SSN or MRN",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Update patient.
   * @return Result.
   */
  })
  public ResponseEntity<PatientResponse> updatePatient(
  @Parameter(description = "Patient unique identifier", example = "123", required = true) @PathVariable Long id,
  @RequestBody(description = "Patient update payload", required = true,
  content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientUpdateRequest.class),
  examples = @ExampleObject(value = "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"insurance\":{\"provider\":\"Acme\",\"policyNumber\":\"POL98765\"}}")))
  @Valid @org.springframework.web.bind.annotation.RequestBody PatientUpdateRequest request) {
    return ResponseEntity.ok(patientService.updatePatient(id, request));
  }

  @PatchMapping("/{id}/deactivate")
  @Operation(summary = "Deactivate patient",
  description = "Soft-deactivate a patient record; records remain for auditing but are excluded from active lists.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Patient deactivated successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "404", description = "Patient not found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Deactivate patient.
   * @param @Parameter @ parameter.
   * @return Result.
   */
  })
  public ResponseEntity<Void> deactivatePatient(@Parameter(description = "Patient unique identifier", example = "123", required = true) @PathVariable final Long id) {
    patientService.deactivatePatient(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/reactivate")
  @Operation(summary = "Reactivate patient",
  description = "Re-activate a previously deactivated patient record.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Patient reactivated successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "404", description = "Patient not found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Reactivate patient.
   * @param @Parameter @ parameter.
   * @return Result.
   */
  })
  public ResponseEntity<Void> reactivatePatient(@Parameter(description = "Patient unique identifier", example = "123", required = true) @PathVariable final Long id) {
    patientService.reactivatePatient(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete patient",
  description = "Permanently delete a patient record. Use with caution; typically restricted to administrators.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
    @ApiResponse(responseCode = "404", description = "Patient not found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  /**
   * Delete patient.
   * @param @Parameter @ parameter.
   * @return Result.
   */
  })
  public ResponseEntity<Void> deletePatient(@Parameter(description = "Patient unique identifier", example = "123", required = true) @PathVariable final Long id) {
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
  }
  /**
   * Get patients with allergies.
   * @return Result.
   */

  @GetMapping("/allergies")
  public ResponseEntity<List<PatientResponse>> getPatientsWithAllergies() {
    return ResponseEntity.ok(patientService.getPatientsWithAllergies());
  }

  @GetMapping("/condition/{condition}")
  @Operation(summary = "List patients by medical condition",
  description = "Return patients that have a specified medical condition.")
  /**
   * Get by condition.
   * @param @Parameter @ parameter.
   * @return Result.
   */
  public ResponseEntity<List<PatientResponse>> getByCondition(@Parameter(description = "Medical condition to filter by", example = "diabetes", required = true) @PathVariable final String condition) {
    return ResponseEntity.ok(patientService.getPatientsByMedicalCondition(condition));
  }

  @GetMapping("/insurance/{provider}")
  @Operation(summary = "List patients by insurance provider",
  description = "Return patients covered by the given insurance provider.")
  /**
   * Get by insurance.
   * @param @Parameter @ parameter.
   * @return Result.
   */
  public ResponseEntity<List<PatientResponse>> getByInsurance(@Parameter(description = "Insurance provider name", example = "Acme Health", required = true) @PathVariable final String provider) {
    return ResponseEntity.ok(patientService.getPatientsByInsuranceProvider(provider));
  }
  /**
   * Get statistics.
   * @return Result.
   */

  @GetMapping("/statistics")
  public ResponseEntity<PatientService.PatientStatistics> getStatistics() {
    return ResponseEntity.ok(patientService.getPatientStatistics());
  }
  /**
   * Generate mrn.
   * @return Result.
   */

  @GetMapping("/mrn/generate")
  public ResponseEntity<Map<String, String>> generateMrn() {
    String mrn = patientService.generateMedicalRecordNumber();
    return ResponseEntity.ok(Map.of("mrn", mrn));
  }
  /**
   * Check mrn exists.
   * @param mrn Mrn.
   * @return Result.
   */

  @GetMapping("/mrn/{mrn}/exists")
  public ResponseEntity<Map<String, Boolean>> checkMrnExists(@PathVariable final String mrn) {
    boolean exists = patientService.existsByMedicalRecordNumber(mrn);
    return ResponseEntity.ok(Map.of("exists", exists));
  }
}
