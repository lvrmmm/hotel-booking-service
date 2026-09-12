package ru.lvrmmm.hotelbookingservice.user.dto.response;

import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth,
        UserRole role
){
    public static UserResponse from(User user){
        return new UserResponse(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getRole()
        );
    }
}
