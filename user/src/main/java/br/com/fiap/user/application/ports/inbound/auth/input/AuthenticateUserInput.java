package br.com.fiap.user.application.ports.inbound.auth.input;

public record AuthenticateUserInput(String login, String password) {}