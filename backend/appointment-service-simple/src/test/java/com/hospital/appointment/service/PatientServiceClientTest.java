package com.hospital.appointment.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hospital.appointment.client.PatientServiceClient;
import com.hospital.common.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked"})
public class PatientServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PatientServiceClient patientServiceClient;

    @BeforeEach
    void setUp() {
        // set base URL used by PatientServiceClient
        ReflectionTestUtils.setField(patientServiceClient, "patientServiceUrl", "http://localhost:8081");
    }

    @Test
    void test_validatePatientExists_returnsTrue_whenPatientFound() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", 1), HttpStatus.OK));

        boolean ok = patientServiceClient.validatePatientExists(1L);
        assertTrue(ok);
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void test_validatePatientExists_returnsFalse_when404() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        boolean ok = patientServiceClient.validatePatientExists(1L);
        assertFalse(ok);
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void test_validatePatientExists_throwsException_when5xx() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ValidationException ex = assertThrows(ValidationException.class, () -> patientServiceClient.validatePatientExists(1L));
        assertEquals("Patient service unavailable", ex.getMessage());
    }

    @Test
    void test_validatePatientExists_throwsException_whenNetworkError() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Network error"));

        ValidationException ex = assertThrows(ValidationException.class, () -> patientServiceClient.validatePatientExists(1L));
        assertEquals("Patient service unavailable", ex.getMessage());
    }

    @Test
    void test_getPatientName_returnsFormattedName_whenSuccess() {
        String url = "http://localhost:8081/api/patients/1";
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "John");
        body.put("lastName", "Doe");
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        String name = patientServiceClient.getPatientName(1L);
        assertEquals("John Doe", name);
    }

    @Test
    void test_getPatientName_returnsFallback_whenError() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class))).thenThrow(new RestClientException("boom"));

        String name = patientServiceClient.getPatientName(1L);
        assertEquals("Patient #1", name);
    }

    @Test
    void test_getPatientName_caching() {
        String url = "http://localhost:8081/api/patients/1";
        Map<String, Object> body = Map.of("firstName", "John", "lastName", "Doe");
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        // first call -> hits RestTemplate
        String name1 = patientServiceClient.getPatientName(1L);
        // second call -> should be cached by client implementation
        String name2 = patientServiceClient.getPatientName(1L);

        assertEquals("John Doe", name1);
        assertEquals("John Doe", name2);
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void test_isPatientActive_returnsTrue_whenActive() {
        String url = "http://localhost:8081/api/patients/1";
        Map<String, Object> body = Map.of("status", "ACTIVE");
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        assertTrue(patientServiceClient.isPatientActive(1L));
    }

    @Test
    void test_isPatientActive_returnsFalse_whenInactive() {
        String url = "http://localhost:8081/api/patients/1";
        Map<String, Object> body = Map.of("status", "INACTIVE");
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        assertFalse(patientServiceClient.isPatientActive(1L));
    }

    @Test
    void test_isPatientActive_returnsFalse_when404() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertFalse(patientServiceClient.isPatientActive(1L));
    }

    @Test
    void test_isPatientActive_throwsException_when5xx() {
        String url = "http://localhost:8081/api/patients/1";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        ValidationException ex = assertThrows(ValidationException.class, () -> patientServiceClient.isPatientActive(1L));
        assertNotNull(ex);
    }

    @Test
    void test_getPatientsByIds_batchFetch() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        // spy the client to verify internal getPatientName calls
        PatientServiceClient spyClient = spy(patientServiceClient);
        doReturn("A").when(spyClient).getPatientName(1L);
        doReturn("B").when(spyClient).getPatientName(2L);
        doReturn("C").when(spyClient).getPatientName(3L);

        Map<Long, String> result = spyClient.getPatientsByIds(ids);
        assertEquals(3, result.size());
        assertEquals("A", result.get(1L));
        assertEquals("B", result.get(2L));
        assertEquals("C", result.get(3L));

        verify(spyClient, times(3)).getPatientName(anyLong());
    }
}
