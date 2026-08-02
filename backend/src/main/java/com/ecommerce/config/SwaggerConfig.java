package com.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig — OpenAPI 3.0 configuration for auto-generated API documentation.
 *
 * Accessible at: http://localhost:8080/api/swagger-ui.html
 *
 * Features:
 * - JWT Bearer authentication support in Swagger UI
 * - API metadata (title, description, version, contact)
 * - Security scheme definition
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("E-Commerce Platform API")
                .description("""
                        ## Production-Ready E-Commerce REST API
                        
                        Full-featured e-commerce backend with:
                        - JWT Authentication & RBAC
                        - Product Management with variants
                        - Shopping Cart with coupons
                        - Order Management with state machine
                        - Payment integration (Strategy Pattern)
                        - Reviews & Ratings
                        - Admin Dashboard APIs
                        
                        **Authentication**: Use `POST /api/auth/login` to get a Bearer token,
                        then click "Authorize" and enter: `Bearer <your-token>`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("E-Commerce Team")
                        .email("dev@ecommerce.com")
                )
                .license(new License()
                        .name("MIT License")
                );
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT Bearer token (without 'Bearer' prefix in the field below)");
    }
}
