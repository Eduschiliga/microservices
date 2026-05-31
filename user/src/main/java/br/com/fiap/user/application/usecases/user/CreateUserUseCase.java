package br.com.fiap.user.application.usecases.user;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.exceptions.DuplicateFieldException;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.ports.inbound.user.create.ForCreatingUser;
import br.com.fiap.user.application.ports.inbound.user.create.input.CreateUserInput;
import br.com.fiap.user.application.ports.inbound.user.create.output.CreateUserOutput;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.util.Objects;

@Named
public class CreateUserUseCase implements ForCreatingUser {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoder;

    @Inject
    public CreateUserUseCase(final UserRepositoryPort userRepositoryPort, final PasswordEncoderPort passwordEncoder) {
        this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Override
    @Transactional
    public CreateUserOutput create(CreateUserInput createUserInput) {
        validateDuplicate(createUserInput);

        Address address = createAddress(createUserInput);

        User newUser = User.newUser(
                createUserInput.name(),
                createUserInput.email(),
                createUserInput.login(),
                passwordEncoder.encode(createUserInput.password()),
                address,
                createUserInput.userType(),
                createUserInput.roles()
        );
        User createdUser = userRepositoryPort.create(newUser);

        return CreateUserOutput.from(createdUser);
    }

    private void validateDuplicate(CreateUserInput createUserInput) {
        validateDuplicateEmail(createUserInput.email());
        validateDuplicateLogin(createUserInput.login());
    }

    @Override
    public void validateDuplicateLogin(String login) {
        if (userRepositoryPort.existsByLogin(login)) {
            throw new DuplicateFieldException("Login already exists");
        }
    }

    @Override
    public void validateDuplicateEmail(String email) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new DuplicateFieldException("Email already exists");
        }
    }

    private Address createAddress(CreateUserInput createUserInput) {
        Address address = null;

        if (createUserInput.address() != null) {
            address = Address.newAddress(
                    createUserInput.address().street(),
                    createUserInput.address().number(),
                    createUserInput.address().complement(),
                    createUserInput.address().city(),
                    createUserInput.address().state(),
                    createUserInput.address().zipCode()
            );
        }

        return address;
    }
}
