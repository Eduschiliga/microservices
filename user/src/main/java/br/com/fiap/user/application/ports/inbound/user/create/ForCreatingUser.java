package br.com.fiap.user.application.ports.inbound.user.create;

import br.com.fiap.user.application.ports.inbound.user.create.input.CreateUserInput;
import br.com.fiap.user.application.ports.inbound.user.create.output.CreateUserOutput;

public interface ForCreatingUser {
    CreateUserOutput create(CreateUserInput createUserInput);
    void validateDuplicateEmail(String email);

    void validateDuplicateLogin(String login);
}
