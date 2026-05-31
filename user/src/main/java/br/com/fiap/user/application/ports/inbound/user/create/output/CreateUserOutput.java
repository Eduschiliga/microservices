package br.com.fiap.user.application.ports.inbound.user.create.output;

import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;

import java.time.LocalDateTime;
import java.util.Set;

public record CreateUserOutput(
        UserId userId,
        String name,
        String email,
        String login,
        String password,
        CreateAddressOutput address,
        UserType userType,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CreateUserOutput from(User user) {
        return new CreateUserOutput(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getPassword(),
                user.getAddress() != null ? CreateAddressOutput.from(user.getAddress()) : null,
                user.getUserType(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
