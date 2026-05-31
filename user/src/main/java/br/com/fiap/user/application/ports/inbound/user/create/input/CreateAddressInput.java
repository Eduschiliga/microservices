package br.com.fiap.user.application.ports.inbound.user.create.input;

public record CreateAddressInput(
        String street,
        String number,
        String complement,
        String city,
        String state,
        String zipCode
) {
}
