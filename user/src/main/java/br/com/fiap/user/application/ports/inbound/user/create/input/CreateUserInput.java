package br.com.fiap.user.application.ports.inbound.user.create.input;


import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.UserType;

import java.util.Set;

public record CreateUserInput(
        String name,
        String login,
        String email,
        String password,
        CreateAddressInput address,
        UserType userType,
        Set<Role> roles
) {
}
