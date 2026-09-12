package ru.lvrmmm.hotelbookingservice.user.dto.request;

import jakarta.validation.constraints.NotNull;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}
