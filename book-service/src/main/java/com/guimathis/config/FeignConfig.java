package com.guimathis.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
public class FeignConfig {

    private static final Logger logger = LoggerFactory.getLogger(FeignConfig.class);

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String message = "Unknown error";
            try {
                if (response.body() != null) {
                    message = new BufferedReader(new InputStreamReader(response.body().asInputStream()))
                            .lines().collect(Collectors.joining("\n"));
                }
            } catch (Exception e) {
                logger.error("Error decoding feign error response", e);
            }
            
            logger.error("Feign request failed. Method: {}, Status: {}, Body: {}", methodKey, response.status(), message);
            return new RuntimeException(message);
        };
    }
}
