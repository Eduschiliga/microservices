package br.com.fiap.user.application.domain.exceptions;

public class TokenInvalidException extends RuntimeException {
    public TokenInvalidException(String message) {
        super(message, null, true, false);
    }
}
