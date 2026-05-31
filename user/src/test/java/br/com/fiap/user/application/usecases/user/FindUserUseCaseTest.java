package br.com.fiap.user.application.usecases.user;

import br.com.fiap.user.application.domain.exceptions.UserNotFoundException;
import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.domain.user.Role;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.domain.user.UserType;
import br.com.fiap.user.application.ports.inbound.user.get.output.GetUserByIdOutput;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUserOutput;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private FindUserUseCase findUserUseCase;

    @Test
    void shouldFindUserByIdSuccessfully() {
        String idStr = UUID.randomUUID().toString();
        User user = User.with(new UserId(idStr), "John", "john@test.com", "john", "pass", null, UserType.CLIENT, Set.of(Role.USER), null, null);

        when(userRepositoryPort.findById(any(UserId.class))).thenReturn(Optional.of(user));

        GetUserByIdOutput output = findUserUseCase.findUserById(idStr);

        assertNotNull(output);
        assertEquals(idStr, output.userId().value());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String idStr = UUID.randomUUID().toString();
        when(userRepositoryPort.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> findUserUseCase.findUserById(idStr));
    }

    @Test
    void shouldListUsersSuccessfully() {
        User user = User.with(new UserId("1"), "John", "john@test.com", "john", "pass", null, UserType.CLIENT, Set.of(Role.USER), null, null);

        when(userRepositoryPort.find(1, 1))
                .thenReturn(new Pagination<>(1, 1, 1, List.of(user)));

        Pagination<ListUserOutput> result = findUserUseCase.listUsers(1, 1);

        assertFalse(result.items().isEmpty());
        assertEquals(1, result.totalItems());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsInvalidFormat() {
        String invalidId = "invalid-uuid-format";

        assertThrows(IllegalArgumentException.class, () -> findUserUseCase.findUserById(invalidId));
    }

    @Test
    void shouldThrowExceptionWhenUserDomainByIdNotFound() {
        String idStr = UUID.randomUUID().toString();

        when(userRepositoryPort.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> findUserUseCase.findUserDomainById(idStr));
    }

    @Test
    void shouldFindAllByNameSuccessfully() {
        String name = "John";
        int page = 0;
        int perPage = 10;
        User user = User.with(new UserId("1"), "John Doe", "john@test.com", "john", "pass", null, UserType.CLIENT, Set.of(Role.USER), null, null);

        when(userRepositoryPort.findAllByName(page, perPage, name))
                .thenReturn(new Pagination<>(page, perPage, 1, List.of(user)));

        Pagination<ListUserOutput> result = findUserUseCase.findAllByName(page, perPage, name);

        assertNotNull(result);
        assertFalse(result.items().isEmpty());
        assertEquals(1, result.totalItems());
        assertEquals("John Doe", result.items().getFirst().name());

        verify(userRepositoryPort).findAllByName(page, perPage, name);
    }

    @Test
    void shouldReturnEmptyListWhenNoUserFoundByName() {
        String name = "NonExistent";
        int page = 0;
        int perPage = 10;

        when(userRepositoryPort.findAllByName(page, perPage, name))
                .thenReturn(new Pagination<>(page, perPage, 0, List.of()));

        Pagination<ListUserOutput> result = findUserUseCase.findAllByName(page, perPage, name);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());

        verify(userRepositoryPort).findAllByName(page, perPage, name);
    }
}