package ru.lvrmmm.hotelbookingservice.user.dto.request;

import java.time.LocalDate;

public record UpdateProfileRequest(
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth
) {
}