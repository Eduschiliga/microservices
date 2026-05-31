package br.com.fiap.user.application.ports.inbound.user.update.output;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.address.AddressId;

import java.time.LocalDateTime;

public record UpdateAddressOutput(
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
    public static UpdateAddressOutput from(Address user) {
        return new UpdateAddressOutput(
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
