package ru.lvrmmm.hotelbookingservice.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private User existingUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        existingUser = new User("johndoe", "john@example.com", "hashedPassword",
                "John", null, "Doe", LocalDate.of(1990, 1, 1), UserRole.USER);
        existingUser.setId(UUID.randomUUID());
        userDetails = new UserDetailsImpl(existingUser);
    }

    // ---------- register ----------

    @Test
    void register_shouldReturnJwtResponse_whenRequestIsValid() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "johndoe", "john@example.com", "password123",
                "John", null, "Doe", LocalDate.of(1990, 1, 1)
        );
        UserResponse userResponse = UserResponse.from(existingUser);

        when(userService.createUser(request)).thenReturn(userResponse);
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("fake-jwt-token");

        // when
        JwtResponse response = authService.register(request);

        // then
        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.user().username()).isEqualTo("johndoe");
        verify(userService, times(1)).createUser(request);
    }

    // ---------- login ----------

    @Test
    void login_shouldReturnJwtResponse_whenCredentialsAreValid() {
        // given
        LoginRequest request = new LoginRequest("johndoe", "password123");

        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("fake-jwt-token");

        // when
        JwtResponse response = authService.login(request);

        // then
        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.user().username()).isEqualTo("johndoe");
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        // given
        LoginRequest request = new LoginRequest("johndoe", "wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtTokenProvider, never()).generateToken(any());
    }
}