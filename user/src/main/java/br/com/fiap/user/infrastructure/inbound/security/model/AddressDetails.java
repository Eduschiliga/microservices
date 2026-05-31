package br.com.fiap.user.infrastructure.inbound.security.model;

import java.time.LocalDateTime;

public record AddressDetails(
        String addressId,
        String street,
        String number,
        String complement,
        String city,
        String state,
        String zipCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}