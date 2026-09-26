package ru.lvrmmm.hotelbookingservice.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;
import ru.lvrmmm.hotelbookingservice.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // given
        User user = new User("johndoe", "john@example.com", "hashedPassword",
                "John", null, "Doe", LocalDate.of(1990, 1, 1), UserRole.USER);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        // when
        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        // then
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getPassword()).isEqualTo("hashedPassword");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}