package ru.lvrmmm.hotelbookingservice.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UserCreateRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.response.UserResponse;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;
import ru.lvrmmm.hotelbookingservice.user.exception.UserAlreadyExistsException;
import ru.lvrmmm.hotelbookingservice.user.exception.UserNotFoundException;
import ru.lvrmmm.hotelbookingservice.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserCreateRequest validRequest;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        validRequest = new UserCreateRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Иван",
                "Иванов",
                "Иванович",
                LocalDate.of(1990, 1, 1)
        );

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
    }

    @Test
    @DisplayName("createUser - should create user with hashed password and default role")
    void createUser_Success() {

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");

        verify(passwordEncoder).encode("password123");

        verify(userRepository).save(argThat(user ->
                user.getPasswordHash().equals("hashedPassword") &&
                        user.getRole() == UserRole.USER
        ));
    }

    @Test
    @DisplayName("createUser - should throw UserAlreadyExistsException when username exists")
    void createUser_UsernameExists_ThrowsException() {

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username")
                .hasMessageContaining("testuser");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - should throw UserAlreadyExistsException when email exists")
    void createUser_EmailExists_ThrowsException() {

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email")
                .hasMessageContaining("test@example.com");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findUserById - should return user when exists")
    void findUserById_Success() {

        UUID userId = sampleUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("findUserById - should throw UserNotFoundException when user not found")
    void findUserById_NotFound_ThrowsException() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }
}