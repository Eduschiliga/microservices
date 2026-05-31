package br.com.fiap.user.infrastructure.outbound.persistence;

import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import br.com.fiap.user.infrastructure.outbound.persistence.entity.UserJPAEntity;
import br.com.fiap.user.infrastructure.outbound.persistence.repository.UserJPARepository;
import br.com.fiap.user.infrastructure.outbound.persistence.repository.UserJpaSpecification;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRepositoryPortAdapter implements UserRepositoryPort {

    private final UserJPARepository userJPARepository;

    public UserRepositoryPortAdapter(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }

    @Override
    public boolean existsByLogin(String login) {
        return userJPARepository.existsByLogin(login);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJPARepository.existsByEmail(email);
    }

    @Override
    public User create(User user) {
        return save(user);
    }

    private User save(final User user) {
        return userJPARepository.save(UserJPAEntity.of(user)).toUser();
    }

    @Override
    public User update(User user) {
        return save(user);
    }

    @Override
    public Optional<User> findById(UserId anId) {
        return userJPARepository.findById(anId.value()).map(UserJPAEntity::toUser);
    }

    @Override
    public Pagination<User> findAllByName(int pageNumber, int pageSize, String name) {
        final var withPage = Pageable.ofSize(pageSize).withPage(pageNumber);
        final var withSpec = UserJpaSpecification.create(name);

        final var page = userJPARepository.findAll(withSpec, withPage);

        return new Pagination<User>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.map(UserJPAEntity::toUser).toList()
        );
    }

    @Override
    public Pagination<User> find(int pageNumber, int pageSize) {
        final var withPage = Pageable.ofSize(pageSize).withPage(pageNumber);
        final var page = userJPARepository.findAll(withPage);

        return new Pagination<User>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.map(UserJPAEntity::toUser).toList()
        );
    }

    @Override
    public void deleteById(UserId anId) {
        userJPARepository.deleteById(anId.value());
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return userJPARepository.findByLogin(login).map(UserJPAEntity::toUser);
    }
}
