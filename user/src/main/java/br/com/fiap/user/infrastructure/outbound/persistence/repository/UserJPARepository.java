package br.com.fiap.user.infrastructure.outbound.persistence.repository;

import br.com.fiap.user.infrastructure.outbound.persistence.entity.UserJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJPARepository extends JpaRepository<UserJPAEntity, String>,
        JpaSpecificationExecutor<UserJPAEntity> {
    Optional<UserJPAEntity> findByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);
}
