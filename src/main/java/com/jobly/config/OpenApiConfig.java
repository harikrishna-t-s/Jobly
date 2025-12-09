package com.jobly.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Jobly API",
        description = "Job posting and application platform",
        version = "v1",
        contact = @Contact(name = "Jobly Team", email = "admin@jobly.com")
    )
)
public class OpenApiConfig {
}
