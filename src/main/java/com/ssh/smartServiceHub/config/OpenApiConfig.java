package com.ssh.smartServiceHub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartServiceHub API")
                        .version("v1.0")
                        .description("API documentation for SmartServiceHub"));
    }
}
