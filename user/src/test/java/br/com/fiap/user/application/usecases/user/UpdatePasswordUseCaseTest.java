package br.com.fiap.user.application.usecases.user;

import br.com.fiap.user.application.domain.exceptions.InvalidPasswordException;
import br.com.fiap.user.application.domain.exceptions.UserNotFoundException;
import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;
import br.com.fiap.user.application.ports.inbound.user.password.input.UpdatePasswordInput;
import br.com.fiap.user.application.ports.inbound.user.password.output.UpdatePasswordOutput;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private FindUserUseCase findUserUseCase;

    @InjectMocks
    private UpdatePasswordUseCase updatePasswordUseCase;

    @Test
    void shouldUpdatePasswordSuccessfully() {
        String userId = UUID.randomUUID().toString();
        User user = User.with(new UserId(userId), "John", "email", "login", "oldEncoded", null, UserType.CLIENT, Set.of(Role.USER), null, null);
        UpdatePasswordInput input = new UpdatePasswordInput(userId, "newPass", "oldPass");

        when(findUserUseCase.findUserDomainById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);
        when(passwordEncoder.matches("newPass", "oldEncoded")).thenReturn(false);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(userRepositoryPort.update(any(User.class))).thenReturn(user);

        UpdatePasswordOutput output = updatePasswordUseCase.updatePassword(input);

        assertNotNull(output);
        verify(userRepositoryPort).update(user);
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordIsInvalid() {
        String userId = UUID.randomUUID().toString();
        User user = User.with(new UserId(userId), "John", "email", "login", "oldEncoded", null, UserType.CLIENT, Set.of(Role.USER), null, null);
        UpdatePasswordInput input = new UpdatePasswordInput(userId, "newPass", "wrongOldPass");

        when(findUserUseCase.findUserDomainById(userId)).thenReturn(user);
        when(passwordEncoder.matches("wrongOldPass", "oldEncoded")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> updatePasswordUseCase.updatePassword(input));
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsSameAsOld() {
        String userId = UUID.randomUUID().toString();
        User user = User.with(new UserId(userId), "John", "email", "login", "oldEncoded", null, UserType.CLIENT, Set.of(Role.USER), null, null);
        UpdatePasswordInput input = new UpdatePasswordInput(userId, "oldPass", "oldPass");

        when(findUserUseCase.findUserDomainById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);
        // Simulating that the new raw password matches the old encoded hash (logic inside use case)
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);

        assertThrows(InvalidPasswordException.class, () -> updatePasswordUseCase.updatePassword(input));
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsNull() {
        String userId = UUID.randomUUID().toString();
        User user = User.with(new UserId(userId), "John", "email", "login", "oldEncoded", null, UserType.CLIENT, Set.of(Role.USER), null, null);
        UpdatePasswordInput input = new UpdatePasswordInput(userId, null, "oldPass");

        when(findUserUseCase.findUserDomainById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);

        assertThrows(InvalidPasswordException.class, () -> updatePasswordUseCase.updatePassword(input));
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsBlank() {
        String userId = UUID.randomUUID().toString();
        User user = User.with(new UserId(userId), "John", "email", "login", "oldEncoded", null, UserType.CLIENT, Set.of(Role.USER), null, null);
        UpdatePasswordInput input = new UpdatePasswordInput(userId, "   ", "oldPass");

        when(findUserUseCase.findUserDomainById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);

        assertThrows(InvalidPasswordException.class, () -> updatePasswordUseCase.updatePassword(input));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForPasswordUpdate() {
        String userId = UUID.randomUUID().toString();
        UpdatePasswordInput input = new UpdatePasswordInput(userId, "newPass", "oldPass");

        when(findUserUseCase.findUserDomainById(userId)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> updatePasswordUseCase.updatePassword(input));
    }
}