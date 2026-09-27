package ru.lvrmmm.hotelbookingservice.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.lvrmmm.hotelbookingservice.common.config.OpenApiConfig;
import ru.lvrmmm.hotelbookingservice.common.dto.PageResponse;
import ru.lvrmmm.hotelbookingservice.security.UserDetailsImpl;
import ru.lvrmmm.hotelbookingservice.user.dto.request.ChangePasswordRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UpdateProfileRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UpdateUserRoleRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.response.UserResponse;
import ru.lvrmmm.hotelbookingservice.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Authentication", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(PageResponse.from(result));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") UUID id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request
            ){
        return ResponseEntity.ok(userService.updateUserRole(id, request.role()));
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> blockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> unblockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
