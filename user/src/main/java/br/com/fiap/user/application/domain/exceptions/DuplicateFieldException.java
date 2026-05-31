package br.com.fiap.user.application.domain.exceptions;

public class DuplicateFieldException extends RuntimeException {
    public DuplicateFieldException(String message) {
        super(message, null, true, false);
    }
}
