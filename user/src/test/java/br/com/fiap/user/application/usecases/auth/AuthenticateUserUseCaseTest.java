package br.com.fiap.user.application.usecases.auth;

import br.com.fiap.user.application.domain.exceptions.TokenInvalidException;
import br.com.fiap.user.application.domain.exceptions.UserNotFoundException;
import br.com.fiap.user.application.domain.exceptions.UserOrPasswordInvalidException;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.ports.inbound.auth.input.AuthenticateUserInput;
import br.com.fiap.user.application.ports.inbound.auth.output.AuthenticateUserOutput;
import br.com.fiap.user.application.ports.inbound.auth.output.GetUserByTokenOutput;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import br.com.fiap.user.application.ports.outbound.token.TokenGeneratorPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenGeneratorPort tokenGeneratorPort;

    @InjectMocks
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Test
    @DisplayName("Should validate token successfully when token is valid and not expired")
    void shouldValidateTokenSuccessfully() {
        String validToken = "valid-token";
        when(tokenGeneratorPort.isExpiredToken(validToken)).thenReturn(false);

        assertDoesNotThrow(() -> authenticateUserUseCase.validateToken(validToken));

        verify(tokenGeneratorPort, times(1)).isExpiredToken(validToken);
    }

    @Test
    @DisplayName("Should throw TokenInvalidException when token is null")
    void shouldThrowExceptionWhenTokenIsNull() {
        assertThrows(TokenInvalidException.class, () -> authenticateUserUseCase.validateToken(null));
        verifyNoInteractions(tokenGeneratorPort);
    }

    @Test
    @DisplayName("Should throw TokenInvalidException when token is empty")
    void shouldThrowExceptionWhenTokenIsEmpty() {
        assertThrows(TokenInvalidException.class, () -> authenticateUserUseCase.validateToken(""));
        verifyNoInteractions(tokenGeneratorPort);
    }

    @Test
    @DisplayName("Should throw TokenInvalidException when token is expired")
    void shouldThrowExceptionWhenTokenIsExpired() {
        String expiredToken = "expired-token";
        when(tokenGeneratorPort.isExpiredToken(expiredToken)).thenReturn(true);

        assertThrows(TokenInvalidException.class, () -> authenticateUserUseCase.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Should return user output when token and user are valid")
    void shouldReturnUserByTokenSuccessfully() {
        String token = "valid-token";
        String login = "user.test";
        User mockUser = mock(User.class);

        when(tokenGeneratorPort.getSubjectByToken(token)).thenReturn(login);
        when(userRepositoryPort.findByLogin(login)).thenReturn(Optional.of(mockUser));

        GetUserByTokenOutput result = authenticateUserUseCase.getUserByToken(token);

        assertNotNull(result);
        verify(tokenGeneratorPort, times(1)).getSubjectByToken(token);
        verify(userRepositoryPort, times(1)).findByLogin(login);
    }

    @Test
    @DisplayName("Should throw TokenInvalidException when subject (login) from token is null")
    void shouldThrowExceptionWhenSubjectIsNull() {
        String token = "invalid-subject-token";
        when(tokenGeneratorPort.getSubjectByToken(token)).thenReturn(null);

        assertThrows(TokenInvalidException.class, () -> authenticateUserUseCase.getUserByToken(token));
        verify(userRepositoryPort, never()).findByLogin(any());
    }

    @Test
    @DisplayName("Should throw TokenInvalidException when subject (login) from token is empty")
    void shouldThrowExceptionWhenSubjectIsEmpty() {
        String token = "empty-subject-token";
        when(tokenGeneratorPort.getSubjectByToken(token)).thenReturn("");

        assertThrows(TokenInvalidException.class, () -> authenticateUserUseCase.getUserByToken(token));
        verify(userRepositoryPort, never()).findByLogin(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user associated with token does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        String token = "valid-token";
        String login = "unknown.user";

        when(tokenGeneratorPort.getSubjectByToken(token)).thenReturn(login);
        when(userRepositoryPort.findByLogin(login)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authenticateUserUseCase.getUserByToken(token));
    }

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void shouldLoginSuccessfully() {
        // Arrange
        String login = "valid.user";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        String expectedToken = "jwt-token-xyz";

        AuthenticateUserInput input = new AuthenticateUserInput(login, rawPassword);

        User mockUser = mock(User.class);
        when(mockUser.getPassword()).thenReturn(encodedPassword);

        when(userRepositoryPort.findByLogin(login)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(tokenGeneratorPort.generate(mockUser)).thenReturn(expectedToken);

        // Act
        AuthenticateUserOutput result = authenticateUserUseCase.login(input);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.token());

        verify(userRepositoryPort, times(1)).findByLogin(login);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verify(tokenGeneratorPort, times(1)).generate(mockUser);
    }

    @Test
    @DisplayName("Should throw UserOrPasswordInvalidException when user is not found")
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Arrange
        String login = "unknown.user";
        AuthenticateUserInput input = new AuthenticateUserInput(login, "anyPassword");

        when(userRepositoryPort.findByLogin(login)).thenReturn(Optional.empty());

        // Act & Assert
        UserOrPasswordInvalidException exception = assertThrows(
                UserOrPasswordInvalidException.class,
                () -> authenticateUserUseCase.login(input)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepositoryPort, times(1)).findByLogin(login);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(tokenGeneratorPort);
    }

    @Test
    @DisplayName("Should throw UserOrPasswordInvalidException when password does not match")
    void shouldThrowExceptionWhenPasswordIsInvalidDuringLogin() {
        // Arrange
        String login = "valid.user";
        String wrongPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        AuthenticateUserInput input = new AuthenticateUserInput(login, wrongPassword);

        User mockUser = mock(User.class);
        when(mockUser.getPassword()).thenReturn(encodedPassword);

        when(userRepositoryPort.findByLogin(login)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

        // Act & Assert
        UserOrPasswordInvalidException exception = assertThrows(
                UserOrPasswordInvalidException.class,
                () -> authenticateUserUseCase.login(input)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepositoryPort, times(1)).findByLogin(login);
        verify(passwordEncoder, times(1)).matches(wrongPassword, encodedPassword);
        verifyNoInteractions(tokenGeneratorPort);
    }
}