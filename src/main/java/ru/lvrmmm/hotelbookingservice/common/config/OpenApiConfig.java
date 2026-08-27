package ru.lvrmmm.hotelbookingservice.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Hotel Booking Service API",
                version = "v1",
                description = "REST API для управления номерами, бронированиями и пользователями отеля"
        )
)
public class OpenApiConfig {
}