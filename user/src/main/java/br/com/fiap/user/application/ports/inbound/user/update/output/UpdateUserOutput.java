package br.com.fiap.user.application.ports.inbound.user.update.output;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateUserOutput(
        UserId userId,
        String name,
        String login,
        String email,
        String password,
        UpdateAddressOutput address,
        UserType userType,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UpdateUserOutput from(User user) {
        Address address = user.getAddress();

        return new UpdateUserOutput(
                user.getUserId(),
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getPassword(),
                address != null ? new UpdateAddressOutput(
                        address.getAddressId(),
                        address.getStreet(),
                        address.getNumber(),
                        address.getComplement(),
                        address.getCity(),
                        address.getState(),
                        address.getZipCode(),
                        address.getCreatedAt(),
                        address.getUpdatedAt()
                ) : null,
                user.getUserType(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
