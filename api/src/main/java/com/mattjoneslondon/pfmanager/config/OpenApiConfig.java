package com.mattjoneslondon.pfmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class OpenApiConfig {
    private final Optional<BuildProperties> buildProperties;

    public OpenApiConfig(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        final String version = buildProperties.map(BuildProperties::getVersion).orElse("unknown");
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Manager API")
                        .version(version)
                        .description("REST API for managing investment portfolios. Provides endpoints for creating and " +
                                "managing portfolios, tracking holdings, and retrieving market data and valuations."));
    }
}