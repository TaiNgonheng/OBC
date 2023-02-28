package com.rhbgroup.dte.obc.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Open Banking Client", version = "v1"))
@SecurityScheme(
        name = "Bearer Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer"
)
public class OpenApiConfig {
}
