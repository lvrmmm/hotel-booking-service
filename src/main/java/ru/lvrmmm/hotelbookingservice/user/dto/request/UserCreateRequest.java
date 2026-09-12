package ru.lvrmmm.hotelbookingservice.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;

import java.time.LocalDate;

public record UserCreateRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 40, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 40, message = "First name must not exceed 40 characters")
        String firstName,

        @Size(max = 40, message = "Middle name must not exceed 40 characters")
        String middleName,

        @NotBlank(message = "Last name is required")
        @Size(max = 40, message = "Last name must not exceed 50 characters")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth

) {
    public User toEntity(String hashedPassword) {
        return new User(username, email, hashedPassword, firstName, middleName, lastName, dateOfBirth, UserRole.USER);
    }
}