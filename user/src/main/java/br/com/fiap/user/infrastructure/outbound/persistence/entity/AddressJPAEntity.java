package br.com.fiap.user.infrastructure.outbound.persistence.entity;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.address.AddressId;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table
@Entity(name = "addresses")
public class AddressJPAEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id", nullable = false, unique = true, length = 36)
    private String addressId;

    @Column(name = "street")
    private String street;

    @Column(name = "number")
    private String number;

    @Column(name = "complement")
    private String complement;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        setCreatedAt(now);
        setUpdatedAt(now);
    }

    public AddressJPAEntity() {

    }

    public AddressJPAEntity(
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

    public static AddressJPAEntity of(final Address address) {
        if (address == null) return null;

        return new AddressJPAEntity(
                address.getAddressId() != null ? address.getAddressId().value() : null,
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

    public Address toAddress() {
        return Address.with(
                AddressId.from(addressId),
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

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
