package ru.lvrmmm.hotelbookingservice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username length must be from 3 to 50 symbols")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password length must be from 8 to 100 symbols")
        String password
) {
}
