package com.hospital.appointment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.lang.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class RestTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Value("${patient.service.connection-timeout:5000}")
    private int connectTimeoutMs;

    @Value("${patient.service.read-timeout:10000}")
    private int readTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        // Configure Apache HttpClient with timeouts from properties
    @SuppressWarnings("deprecation")
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
        .setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
        .build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(20);
        connManager.setMaxTotal(100);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();
        Objects.requireNonNull(httpClient, "httpClient must not be null");

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        Objects.requireNonNull(factory, "factory must not be null");

        RestTemplate restTemplate = new RestTemplate(factory);

        // Add interceptor to log request/response metadata (method, URI, status, duration)
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new ClientHttpRequestInterceptor() {
            @Override
            public @NonNull ClientHttpResponse intercept(@NonNull org.springframework.http.HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
                long start = System.currentTimeMillis();
                log.debug("Outgoing request {} {} headers={}", request.getMethod(), request.getURI(), request.getHeaders());
                ClientHttpResponse response;
                try {
                    // Wrap response so we can safely read the body for logging without consuming the stream
                    response = new BufferingClientHttpResponse(execution.execute(request, body));
                    long duration = System.currentTimeMillis() - start;
                    // read response body (safe: wrapped)
                    String respBody = Objects.toString(tryReadBody(response), "");
                    log.debug("Incoming response {} ({} ms) headers={} body={}", response.getStatusCode(), duration, response.getHeaders(), respBody);
                    return response;
                } catch (IOException e) {
                    long duration = System.currentTimeMillis() - start;
                    log.warn("Request to {} {} failed after {} ms: {}", request.getMethod(), request.getURI(), duration, e.getMessage());
                    throw e;
                }
            }

            private String tryReadBody(ClientHttpResponse response) {
                try (InputStream is = response.getBody()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    return "<unreadable>";
                }
            }
        });
        restTemplate.setInterceptors(interceptors);

        // Don't throw exceptions on 404 so callers can inspect the response and treat 'not found' as a business result
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                HttpStatusCode status = response.getStatusCode();
                if (status.value() == 404) return false;
                return super.hasError(response);
            }
        });

        return restTemplate;
    }

    // Simple buffering wrapper to allow multiple reads of response body without consuming underlying stream.
    private static class BufferingClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse delegate;
        private final byte[] body;

        BufferingClientHttpResponse(ClientHttpResponse delegate) throws IOException {
            this.delegate = delegate;
            InputStream is = delegate.getBody();
            if (is == null) {
                this.body = new byte[0];
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream in = is) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                }
                this.body = baos.toByteArray();
            }
        }

        @Override
        public @NonNull HttpStatusCode getStatusCode() throws IOException {
            HttpStatusCode status = delegate.getStatusCode();
            return status != null ? status : org.springframework.http.HttpStatus.OK;
        }

        @Deprecated
        @SuppressWarnings("removal")
        @Override
        public int getRawStatusCode() throws IOException {
            return delegate.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            String text = delegate.getStatusText();
            return text != null ? text : "";
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public @NonNull InputStream getBody() {
            // Always return a non-null InputStream (empty stream if body is empty)
            return Objects.requireNonNull(new ByteArrayInputStream(this.body));
        }

        @Override
        public @NonNull HttpHeaders getHeaders() {
            return Objects.requireNonNull(delegate.getHeaders(), "headers must not be null");
        }
    }
}
