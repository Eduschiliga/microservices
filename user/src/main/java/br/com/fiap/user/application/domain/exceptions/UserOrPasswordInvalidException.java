package br.com.fiap.user.application.domain.exceptions;

public class UserOrPasswordInvalidException extends RuntimeException {
    public UserOrPasswordInvalidException(String message) {
        super(message, null, true, false);
    }
}
