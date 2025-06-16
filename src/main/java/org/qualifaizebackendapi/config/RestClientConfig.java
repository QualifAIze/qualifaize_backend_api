package org.qualifaizebackendapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Configuration
public class RestClientConfig {

    @Value("${external-services.document-parser-base-url}")
    private String documentParserBaseUrl;

    private static final int TIMEOUT_MINUTES = 2;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;

    /**
     * RestClient configured specifically for the document parser service
     * with base URL and 2-minute timeout
     */
    @Bean
    public RestClient documentParserRestClient() {
        return RestClient.builder()
                .baseUrl(documentParserBaseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .requestFactory(documentParserRequestFactory())
                .build();
    }

    /**
     * General purpose RestClient without base URL
     * Uses the same timeout configuration for consistency
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .requestFactory(documentParserRequestFactory())
                .build();
    }

    /**
     * HTTP request factory with 2-minute read timeout and 10-second connection timeout
     */
    @Bean
    public ClientHttpRequestFactory documentParserRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS));
        factory.setReadTimeout(Duration.ofMinutes(TIMEOUT_MINUTES));
        return factory;
    }
}