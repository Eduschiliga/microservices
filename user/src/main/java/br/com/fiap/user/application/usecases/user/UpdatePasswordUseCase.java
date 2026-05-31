package br.com.fiap.user.application.usecases.user;

import br.com.fiap.user.application.ports.inbound.user.password.input.UpdatePasswordInput;
import br.com.fiap.user.application.ports.inbound.user.password.ForUpdatingPassword;
import br.com.fiap.user.application.ports.inbound.user.password.output.UpdatePasswordOutput;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import br.com.fiap.user.application.domain.exceptions.InvalidPasswordException;
import br.com.fiap.user.application.domain.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.util.Objects;

@Named
public class UpdatePasswordUseCase implements
        ForUpdatingPassword {

    private final UserRepositoryPort userRepositoryPort;
    private final FindUserUseCase findUserUseCase;
    private final PasswordEncoderPort passwordEncoder;

    @Inject
    public UpdatePasswordUseCase(
            final UserRepositoryPort userRepositoryPort,
            final PasswordEncoderPort passwordEncoder,
            final FindUserUseCase findUserUseCase
    ) {
        this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.findUserUseCase = Objects.requireNonNull(findUserUseCase);
    }

    @Override
    @Transactional
    public UpdatePasswordOutput updatePassword(UpdatePasswordInput input) {
        User user = findUserUseCase.findUserDomainById(input.userId());
        validateNewPassword(input, user.getPassword());

        user.updatePassword(passwordEncoder.encode(input.newPassword()));
        user = userRepositoryPort.update(user);

        return UpdatePasswordOutput.from(user);
    }

    @Override
    public void validateNewPassword(UpdatePasswordInput input, String actualPassword) {
        boolean matchPassword = passwordEncoder.matches(input.oldPassword(), actualPassword);

        if (!matchPassword) {
            throw new InvalidPasswordException("Old password is invalid.");
        }

        if (input.newPassword() == null || input.newPassword().isBlank()) {
            throw new InvalidPasswordException("New password cannot be null or blank.");
        }

        if (passwordEncoder.matches(input.newPassword(), actualPassword)) {
            throw new InvalidPasswordException("New password cannot be the same as old password.");
        }
    }
}
