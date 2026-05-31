package br.com.fiap.user.application.ports.inbound.user.create.output;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.address.AddressId;

import java.time.LocalDateTime;

public record CreateAddressOutput(
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
    public static CreateAddressOutput from(Address address) {
        return new CreateAddressOutput(
                address.getAddressId(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

}
