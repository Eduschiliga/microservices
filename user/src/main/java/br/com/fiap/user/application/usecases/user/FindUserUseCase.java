package br.com.fiap.user.application.usecases.user;

import br.com.fiap.user.application.domain.exceptions.UserNotFoundException;
import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.ports.inbound.user.get.ForGettingUserById;
import br.com.fiap.user.application.ports.inbound.user.get.ForGettingUserDomainById;
import br.com.fiap.user.application.ports.inbound.user.get.output.GetUserByIdOutput;
import br.com.fiap.user.application.ports.inbound.user.list.ForListingUser;
import br.com.fiap.user.application.ports.inbound.user.list.ForListingUsersByName;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUserOutput;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Objects;

@Named
public class FindUserUseCase implements
        ForGettingUserById,
        ForListingUser,
        ForListingUsersByName,
        ForGettingUserDomainById
{
    private final UserRepositoryPort userRepositoryPort;

    @Inject
    public FindUserUseCase(final UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
    }

    @Override
    public GetUserByIdOutput findUserById(String inputId) {
        User user = findUserDomainById(inputId);

        return GetUserByIdOutput.from(user);
    }

    @Override
    public User findUserDomainById(String inputId) {
        UserId userId = UserId.from(inputId);

        return userRepositoryPort.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User with ID %s not found.".formatted(inputId))
        );
    }

    @Override
    public Pagination<ListUserOutput> listUsers(int pageNumber, int pageSize) {
        return userRepositoryPort
                .find(pageNumber, pageSize)
                .mapItems(ListUserOutput::from);
    }



    @Override
    public Pagination<ListUserOutput> findAllByName(int pageNumber, int pageSize, String name) {
        return userRepositoryPort
                .findAllByName(pageNumber, pageSize, name)
                .mapItems(ListUserOutput::from);
    }
}
