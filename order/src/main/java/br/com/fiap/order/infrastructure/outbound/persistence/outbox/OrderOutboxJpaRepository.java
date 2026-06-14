package br.com.fiap.order.infrastructure.outbound.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutboxJpaEntity, UUID> {
    List<OrderOutboxJpaEntity> findTop10ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus status, Instant now);

    long countByStatus(OutboxStatus status);
}
