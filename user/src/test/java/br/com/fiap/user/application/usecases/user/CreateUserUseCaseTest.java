package br.com.fiap.user.application.usecases.user;

import br.com.fiap.user.application.ports.inbound.user.create.input.CreateAddressInput;
import br.com.fiap.user.application.ports.inbound.user.create.input.CreateUserInput;
import br.com.fiap.user.application.ports.inbound.user.create.output.CreateUserOutput;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @Test
    void shouldCreateUserSuccessfully() {
        CreateAddressInput addressInput = new CreateAddressInput(
                "Rua Teste", "123", "Apto 1", "Lisboa", "Lisboa", "1000-000"
        );
        CreateUserInput input = new CreateUserInput(
                "John Doe", "john", "john@email.com", "123456", addressInput, UserType.CLIENT, Set.of(Role.USER)
        );

        User savedUser = User.with(
                new UserId("user-id-123"), "John Doe", "john@email.com", "john", "encodedPass",
                null, UserType.CLIENT, Set.of(Role.USER), LocalDateTime.now(), LocalDateTime.now()
        );

        when(passwordEncoder.encode(input.password())).thenReturn("encodedPass");
        when(userRepositoryPort.create(any(User.class))).thenReturn(savedUser);

        CreateUserOutput output = createUserUseCase.create(input);

        assertNotNull(output);
        assertEquals(savedUser.getUserId(), output.userId());
        assertEquals("encodedPass", savedUser.getPassword());
        verify(userRepositoryPort, times(1)).create(any(User.class));
    }
}