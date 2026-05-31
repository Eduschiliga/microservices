package br.com.fiap.user.application.ports.inbound.auth;

import br.com.fiap.user.application.ports.inbound.auth.input.AuthenticateUserInput;
import br.com.fiap.user.application.ports.inbound.auth.output.AuthenticateUserOutput;

public interface ForAuthenticateUser {
    AuthenticateUserOutput login(AuthenticateUserInput input);
}
