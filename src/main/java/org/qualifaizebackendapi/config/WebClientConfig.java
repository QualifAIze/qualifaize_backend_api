package org.qualifaizebackendapi.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class WebClientConfig {

    @Value("${external-services.document-parser-base-url}")
    private String documentParserBaseUrl;

    private final Integer TIMEOUT_TIME_IN_SECONDS = 120;

    @Bean
    public WebClient documentParserServiceWebClient(WebClient.Builder webClientBuilder) {
        return createWebClient(webClientBuilder, documentParserBaseUrl);
    }

    private WebClient createWebClient(WebClient.Builder webClientBuilder, String baseUrl) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
                })
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024); // 50MB
                })
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(TIMEOUT_TIME_IN_SECONDS))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)))
                .build();
    }
}