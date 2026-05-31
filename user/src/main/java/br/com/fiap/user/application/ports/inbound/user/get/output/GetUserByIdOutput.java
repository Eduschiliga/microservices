package br.com.fiap.user.application.ports.inbound.user.get.output;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;

import java.time.LocalDateTime;
import java.util.Set;

public record GetUserByIdOutput(
        UserId userId,
        String name,
        String email,
        String login,
        GetAddressOutput address,
        UserType userType,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static GetUserByIdOutput from(User user) {
        Address address = user.getAddress();
        return new GetUserByIdOutput(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                address != null ? new GetAddressOutput(
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
