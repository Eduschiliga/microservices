package br.com.fiap.user.application.ports.inbound.user.password.output;

import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdatePasswordOutput(
        UserId userId,
        String name,
        String login,
        String email,
        String password,
        Address address,
        UserType userType,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UpdatePasswordOutput from(User user) {
        return new UpdatePasswordOutput(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getPassword(),
                user.getAddress(),
                user.getUserType(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}