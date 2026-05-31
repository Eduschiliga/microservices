package br.com.fiap.user.application.ports.inbound.user.get.output;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.address.AddressId;

import java.time.LocalDateTime;

public record GetAddressOutput(
        AddressId addressId,
        String street,
        String number,
        String complement,
        String city,
        String state,
        String zipCode,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GetAddressOutput from(Address user) {
        return new GetAddressOutput(
                user.getAddressId(),
                user.getStreet(),
                user.getNumber(),
                user.getComplement(),
                user.getCity(),
                user.getState(),
                user.getZipCode(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
