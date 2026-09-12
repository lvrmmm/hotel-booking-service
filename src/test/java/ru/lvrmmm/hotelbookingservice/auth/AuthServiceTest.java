package ru.lvrmmm.hotelbookingservice.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.lvrmmm.hotelbookingservice.auth.dto.JwtResponse;
import ru.lvrmmm.hotelbookingservice.auth.dto.LoginRequest;
import ru.lvrmmm.hotelbookingservice.security.CustomUserDetailsService;
import ru.lvrmmm.hotelbookingservice.security.JwtTokenProvider;
import ru.lvrmmm.hotelbookingservice.security.UserDetailsImpl;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UserCreateRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.response.UserResponse;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;
import ru.lvrmmm.hotelbookingservice.user.service.UserService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private UserCreateRequest registerRequest;
    private LoginRequest loginRequest;
    private User sampleUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = new UserCreateRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Иван",
                "Иванов",
                "Иванович",
                LocalDate.of(1990, 1, 1)
        );

        loginRequest = new LoginRequest("testuser", "password123");

        sampleUser = new User(
                "testuser",
                "test@example.com",
                "hashedPassword",
                "Иван",
                "Иванов",
                "Иванович",
                LocalDate.of(1990, 1, 1),
                UserRole.USER
        );
        sampleUser.setId(UUID.randomUUID());

        userDetails = new UserDetailsImpl(sampleUser);
    }

    @Test
    @DisplayName("register - should create user and return JWT token")
    void register_Success() {

        UserResponse userResponse = UserResponse.from(sampleUser);
        String expectedToken = "jwt-token-12345";

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn(expectedToken);

        JwtResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(expectedToken);
        assertThat(response.user()).isEqualTo(userResponse);

        verify(userService).createUser(registerRequest);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtTokenProvider).generateToken(userDetails);
    }

    @Test
    @DisplayName("login - should authenticate user and return JWT token")
    void login_Success() {

        String expectedToken = "jwt-token-67890";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn(expectedToken);

        JwtResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(expectedToken);
        assertThat(response.user().username()).isEqualTo("testuser");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtTokenProvider).generateToken(userDetails);
    }

    @Test
    @DisplayName("login - should throw BadCredentialsException when password is wrong")
    void login_WrongPassword_ThrowsException() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - should throw BadCredentialsException when user not found")
    void login_UserNotFound_ThrowsException() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtTokenProvider, never()).generateToken(any());
    }
}