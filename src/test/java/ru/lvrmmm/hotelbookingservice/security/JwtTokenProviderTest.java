package ru.lvrmmm.hotelbookingservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            "NDA0RTYzNTI2NjU0NmE1NzZlNWE3MjM0NzUzNzc4MjE0MTI1NDQyQTQ3MkQ0QjYxNTA2NDUzNjc1NmQ1ODAw";

    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, 3600000L);

        User user = new User("johndoe", "john@example.com", "hash", "John", null, "Doe",
                LocalDate.of(1990, 1, 1), UserRole.USER);
        user.setId(UUID.randomUUID());
        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void generateToken_shouldReturnNonEmptyToken() {
        // when
        String token = jwtTokenProvider.generateToken(userDetails);

        // then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        // given
        String token = jwtTokenProvider.generateToken(userDetails);

        // when
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(username).isEqualTo("johndoe");
    }

    @Test
    void validateToken_shouldReturnTrue_forFreshlyGeneratedToken() {
        // given
        String token = jwtTokenProvider.generateToken(userDetails);

        // when / then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forMalformedToken() {
        assertThat(jwtTokenProvider.validateToken("not-a-real-token")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenSignedWithDifferentSecret() {
        // given: токен, сгенерированный другим экземпляром провайдера с другим секретом
        String differentSecret =
                "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
        JwtTokenProvider otherProvider = new JwtTokenProvider(differentSecret, 3600000L);
        String tokenFromOtherSecret = otherProvider.generateToken(userDetails);

        // when / then
        assertThat(jwtTokenProvider.validateToken(tokenFromOtherSecret)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenExpired() throws InterruptedException {
        // given: провайдер с очень коротким временем жизни токена
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(TEST_SECRET, 1L);
        String token = shortLivedProvider.generateToken(userDetails);

        Thread.sleep(50); // ждём, пока токен точно истечёт

        // when / then
        assertThat(shortLivedProvider.validateToken(token)).isFalse();
    }
}