package br.com.fiap.user.application.ports.outbound.repository;

import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;

import java.util.Optional;

public interface UserRepositoryPort {
    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    User create(User aUser);

    User update(User aUser);

    Optional<User> findById(UserId anId);

    Pagination<User> findAllByName(int pageSize, int pageNumber, String name);

    Pagination<User> find(int page, int size);

    void deleteById(UserId anId);

    Optional<User> findByLogin(String login);
}
