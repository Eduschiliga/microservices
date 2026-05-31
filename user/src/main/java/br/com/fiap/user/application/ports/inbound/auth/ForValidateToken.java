package br.com.fiap.user.application.ports.inbound.auth;

public interface ForValidateToken {
    void validateToken(String token);
}
