package br.com.fiap.user.application.domain.user.address;

import java.time.LocalDateTime;

public class Address {
    private AddressId addressId;
    private String street;
    private String number;
    private String complement;
    private String city;
    private String state;
    private String zipCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Address(
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
        this.addressId = addressId;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Address with(
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
        return new Address(
                addressId,
                street,
                number,
                complement,
                city,
                state,
                zipCode,
                createdAt,
                updatedAt
        );
    }

    public static Address newAddress(
            String street,
            String number,
            String complement,
            String city,
            String state,
            String zipCode
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new Address(
                new AddressId(null),
                street,
                number,
                complement,
                city,
                state,
                zipCode,
                now,
                now
        );
    }

    public Address update(
            String street,
            String number,
            String complement,
            String city,
            String state,
            String zipCode
    ) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    public AddressId getAddressId() {
        return addressId;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
