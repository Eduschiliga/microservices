package br.com.fiap.user.application.domain.user.address;

import java.util.UUID;

public record AddressId(String value) {

    public static AddressId from(final String value) {
        try {
            return new AddressId(UUID.fromString(value).toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Address Id");
        }
    }

    public static AddressId generate() {
        return new AddressId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }

}
