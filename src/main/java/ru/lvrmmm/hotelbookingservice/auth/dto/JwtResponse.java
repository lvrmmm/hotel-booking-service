package ru.lvrmmm.hotelbookingservice.auth.dto;

import ru.lvrmmm.hotelbookingservice.user.dto.response.UserResponse;

public record JwtResponse (
        String token,
        UserResponse user
){
}
