package com.mattjoneslondon.pfmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    private final BuildProperties buildProperties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Manager API")
                        .version(buildProperties.getVersion())
                        .description("REST API for managing investment portfolios. Provides endpoints for creating and " +
                                "managing portfolios, tracking holdings, and retrieving market data and valuations."));
    }
}