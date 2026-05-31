package br.com.fiap.user.application.ports.inbound.auth;

import br.com.fiap.user.application.ports.inbound.auth.output.GetUserByTokenOutput;

public interface ForGettingUserByToken {

    GetUserByTokenOutput getUserByToken(String token);
}
