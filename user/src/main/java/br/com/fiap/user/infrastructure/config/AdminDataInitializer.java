package br.com.fiap.user.infrastructure.config;

import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public AdminDataInitializer(UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepositoryPort.findByLogin("admin").ifPresentOrElse(
                this::ensureAdminRole,
                this::createAdmin
        );
    }

    private void ensureAdminRole(User admin) {
        if (admin.getRoles().contains(Role.ADMIN)) {
            return;
        }
        User updated = User.with(
                admin.getUserId(), admin.getName(), admin.getEmail(), admin.getLogin(),
                admin.getPassword(), admin.getAddress(), admin.getUserType(),
                Set.of(Role.ADMIN),
                admin.getCreatedAt(), admin.getUpdatedAt()
        );
        userRepositoryPort.update(updated);
    }

    private void createAdmin() {
        LocalDateTime now = LocalDateTime.now();
        User admin = User.with(
                new UserId(null), "Admin", "admin@restaurant.com", "admin",
                passwordEncoderPort.encode("Admin@123"),
                null, UserType.CLIENT, Set.of(Role.ADMIN), now, now
        );
        userRepositoryPort.create(admin);
    }
}
