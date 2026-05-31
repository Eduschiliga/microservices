package br.com.fiap.user.infrastructure.outbound.persistence.repository;

import br.com.fiap.user.infrastructure.outbound.persistence.entity.UserJPAEntity;
import org.springframework.data.jpa.domain.Specification;

public class UserJpaSpecification {

    private UserJpaSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<UserJPAEntity> create(final String name) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (name != null && !name.isBlank()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%")
                );
            }

            return predicates;
        };
    }
}
