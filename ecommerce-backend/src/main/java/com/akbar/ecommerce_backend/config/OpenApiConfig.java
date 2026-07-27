package com.akbar.ecommerce_backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(){

        return new OpenAPI()
                .info(
                        new Info()
                                .title("E-Commerce Backend API")
                                .description("Production-ready REST APIs for User, Authentication, Product and Order Management.")
                                .version("v1.0")
                                .contact(
                                        new Contact()
                                                .name("Muhammad Akbar")
                                                .email("mhdakbar.ak@gmail.com")
                                )
                                .license(
                        new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                )
                );
    }
}
