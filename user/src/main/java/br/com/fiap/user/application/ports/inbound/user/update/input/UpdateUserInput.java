package br.com.fiap.user.application.ports.inbound.user.update.input;

import br.com.fiap.user.application.domain.user.UserType;

public record UpdateUserInput(
        String userId,
        String name,
        String login,
        String email,
        UpdateAddressInput address,
        UserType userType
) {

}
