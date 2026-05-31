package br.com.fiap.user.application.domain.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message, null, true, false);
    }
}
