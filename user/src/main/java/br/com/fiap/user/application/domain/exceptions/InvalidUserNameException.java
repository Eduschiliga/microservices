package br.com.fiap.user.application.domain.exceptions;

public class InvalidUserNameException extends RuntimeException {
    public InvalidUserNameException(String message) {
        super(message, null, true, false);
    }
}
