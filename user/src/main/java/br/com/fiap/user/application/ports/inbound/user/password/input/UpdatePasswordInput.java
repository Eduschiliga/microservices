package br.com.fiap.user.application.ports.inbound.user.password.input;


public record UpdatePasswordInput(
        String userId,
        String newPassword,
        String oldPassword
) {
}
