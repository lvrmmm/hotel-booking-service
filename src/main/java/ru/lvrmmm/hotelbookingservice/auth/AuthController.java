package ru.lvrmmm.hotelbookingservice.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lvrmmm.hotelbookingservice.auth.dto.JwtResponse;
import ru.lvrmmm.hotelbookingservice.auth.dto.LoginRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UserCreateRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API для регистрации и входа пользователей")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody UserCreateRequest request){
        JwtResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request){
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
