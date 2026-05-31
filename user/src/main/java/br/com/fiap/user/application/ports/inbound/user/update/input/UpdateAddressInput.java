package br.com.fiap.user.application.ports.inbound.user.update.input;

import br.com.fiap.user.application.domain.user.address.AddressId;

public record UpdateAddressInput(
        AddressId addressId,
        String street,
        String number,
        String complement,
        String city,
        String state,
        String zipCode
) {
}
