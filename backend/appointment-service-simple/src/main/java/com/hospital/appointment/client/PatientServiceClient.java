package com.hospital.appointment.client;

import com.hospital.common.exception.ValidationException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class PatientServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceClient.class);

    private final RestTemplate restTemplate;

    // Explicit constructor replacing Lombok's @RequiredArgsConstructor to ensure IDEs and builds without Lombok work
    public PatientServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${patient.service.url:http://localhost:8081}")
    private String patientServiceUrl;

    public boolean validatePatientExists(@NonNull Long patientId) {
        Objects.requireNonNull(patientId, "patientId must not be null");
        String url = String.format("%s/api/v1/patients/%d", patientServiceUrl, patientId);
        Objects.requireNonNull(patientServiceUrl, "patientServiceUrl must not be null");
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(HttpMethod.GET, "httpMethod must not be null");
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return resp.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            // Patient not found -> treat as not existing
            log.debug("Patient {} not found (404)", patientId);
            return false;
        } catch (HttpClientErrorException e) {
            // other 4xx from patient service - treat as not found for validation
            log.warn("Client error when validating patient {}: {}", patientId, e.getMessage());
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Patient service server error when validating patient {}: {}", patientId, e.getMessage());
            throw new ValidationException("Patient service unavailable");
        } catch (RestClientException e) {
            log.error("Patient service unavailable when validating patient {}: {}", patientId, e.getMessage());
            throw new ValidationException("Patient service unavailable");
        }
    }

    @Cacheable(value = "patient-names", key = "#patientId")
    public String getPatientName(@NonNull Long patientId) {
        Objects.requireNonNull(patientId, "patientId must not be null");
        String url = String.format("%s/api/v1/patients/%d", patientServiceUrl, patientId);
        Objects.requireNonNull(patientServiceUrl, "patientServiceUrl must not be null");
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(HttpMethod.GET, "httpMethod must not be null");
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> body = Objects.requireNonNull(resp.getBody());
                Object fn = body.get("firstName");
                Object ln = body.get("lastName");
                return String.format("%s %s", 
                    Objects.toString(fn, ""), 
                    Objects.toString(ln, "")).trim();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch patient name for {}: {}", patientId, e.getMessage());
        }
        return "Patient #" + patientId;
    }

    public boolean isPatientActive(@NonNull Long patientId) {
        Objects.requireNonNull(patientId, "patientId must not be null");
        String url = String.format("%s/api/v1/patients/%d", patientServiceUrl, patientId);
        Objects.requireNonNull(patientServiceUrl, "patientServiceUrl must not be null");
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(HttpMethod.GET, "httpMethod must not be null");
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> body = Objects.requireNonNull(resp.getBody());
                Object status = body.get("status");
                return "ACTIVE".equalsIgnoreCase(Objects.toString(status, ""));
            }
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            // Not found -> not active
            log.debug("Patient {} not found when checking active status (404)", patientId);
            return false;
        } catch (HttpClientErrorException e) {
            log.warn("Client error when checking active status for {}: {}", patientId, e.getMessage());
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Patient service server error when checking active status for {}: {}", patientId, e.getMessage());
            throw new ValidationException("Patient service unavailable");
        } catch (RestClientException e) {
            log.error("Patient service unavailable when checking active status for {}: {}", patientId, e.getMessage());
            throw new ValidationException("Patient service unavailable");
        }
    }

    public Map<Long, String> getPatientsByIds(@NonNull Collection<Long> ids) {
        Objects.requireNonNull(ids, "ids must not be null");
        Map<Long, String> result = new HashMap<>();
        for (Long id : ids) {
            if (id != null) {
                result.put(id, getPatientName(id));
            }
        }
        return result;
    }
}
