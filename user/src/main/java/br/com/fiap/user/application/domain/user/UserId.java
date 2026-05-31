package br.com.fiap.user.application.domain.user;

import java.util.UUID;

public record UserId(String value) {

    public static UserId from(final String value) {
        try {
            return new UserId(UUID.fromString(value).toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid User Id");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }

}
