package br.com.fiap.user.infrastructure.inbound.rest;

import br.com.fiap.user.api.UsersApi;
import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.ports.inbound.user.create.ForCreatingUser;
import br.com.fiap.user.application.ports.inbound.user.create.input.CreateUserInput;
import br.com.fiap.user.application.ports.inbound.user.create.output.CreateUserOutput;
import br.com.fiap.user.application.ports.inbound.user.get.ForGettingUserById;
import br.com.fiap.user.application.ports.inbound.user.get.output.GetUserByIdOutput;
import br.com.fiap.user.application.ports.inbound.user.list.ForListingUser;
import br.com.fiap.user.application.ports.inbound.user.list.ForListingUsersByName;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUserOutput;
import br.com.fiap.user.application.ports.inbound.user.password.ForUpdatingPassword;
import br.com.fiap.user.application.ports.inbound.user.password.input.UpdatePasswordInput;
import br.com.fiap.user.application.ports.inbound.user.password.output.UpdatePasswordOutput;
import br.com.fiap.user.application.ports.inbound.user.update.UpdateUserInputPort;
import br.com.fiap.user.application.ports.inbound.user.update.input.UpdateUserInput;
import br.com.fiap.user.application.ports.inbound.user.update.output.UpdateUserOutput;
import br.com.fiap.user.infrastructure.inbound.rest.mapper.UserMapper;
import br.com.fiap.user.model.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
public class UserController implements UsersApi {
    private final ForCreatingUser forCreatingUser;
    private final UpdateUserInputPort updateUserInputPort;
    private final ForListingUser forListingUser;
    private final ForListingUsersByName forListingUsersByName;
    private final ForGettingUserById forGettingUserById;
    private final ForUpdatingPassword forUpdatingPassword;

    private final UserMapper userMapper;

    public UserController(
            ForCreatingUser forCreatingUser,
            UpdateUserInputPort updateUserInputPort,
            ForListingUser forListingUser,
            ForGettingUserById forGettingUserById,
            ForListingUsersByName forListingUsersByName,
            ForUpdatingPassword forUpdatingPassword,
            UserMapper userMapper
    ) {
        this.forCreatingUser = forCreatingUser;
        this.updateUserInputPort = updateUserInputPort;
        this.forListingUser = forListingUser;
        this.forGettingUserById = forGettingUserById;
        this.forListingUsersByName = forListingUsersByName;
        this.forUpdatingPassword = forUpdatingPassword;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        CreateUserInput useCaseInput = userMapper.fromDTO(createUserDTO);
        CreateUserOutput useCaseOutput = forCreatingUser.create(useCaseInput);

        URI uri = URI.create("/users/" + useCaseOutput.userId());
        return ResponseEntity.created(uri).body(userMapper.toDTO(useCaseOutput));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.userId == #userId.toString()")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserDTO userDTO) {
        UpdateUserInput useCaseInput = userMapper.fromUpdateDTO(userDTO, userId);
        UpdateUserOutput useCaseOutput = updateUserInputPort.updateUser(useCaseInput);

        return ResponseEntity.ok(userMapper.toDTO(useCaseOutput));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.userId == #userId.toString()")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        GetUserByIdOutput useCaseOutput = forGettingUserById.findUserById(userId.toString());

        return ResponseEntity.ok().body(userMapper.toDTO(useCaseOutput));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedUsersDTO> listUsers(final Integer page, final Integer perPage) {
        Pagination<ListUserOutput> pagination = forListingUser.listUsers(page, perPage);

        return buildListUsers(pagination);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedUsersDTO> listUsersByName(String name, final Integer page, final Integer perPage) {
        Pagination<ListUserOutput> pagination = forListingUsersByName.findAllByName(page, perPage, name);

        return buildListUsers(pagination);
    }

    private ResponseEntity<PaginatedUsersDTO> buildListUsers(Pagination<ListUserOutput> pagination) {
        long totalPages = (long) Math.ceil((double) pagination.totalItems() / pagination.pageSize());

        final PaginatedUsersDTO dto = new PaginatedUsersDTO()
                .page(pagination.currentPage())
                .perPage(pagination.pageSize())
                .totalPages(totalPages)
                .items(pagination.items().stream()
                        .map(userMapper::toDTO)
                        .toList()
                );

        return ResponseEntity.ok(dto);
    }

    @Override
    @PreAuthorize("authentication.principal.userId == #userId.toString()")
    public ResponseEntity<UserDTO> updatePassword(@PathVariable UUID userId, @RequestBody UpdatePasswordDTO updatePasswordDto) {
        UpdatePasswordInput useCaseInput = userMapper.fromUpdatePasswordDTO(updatePasswordDto, userId);
        UpdatePasswordOutput useCaseOutput = forUpdatingPassword.updatePassword(useCaseInput);

        return ResponseEntity.ok(userMapper.toDTO(useCaseOutput));
    }
}
