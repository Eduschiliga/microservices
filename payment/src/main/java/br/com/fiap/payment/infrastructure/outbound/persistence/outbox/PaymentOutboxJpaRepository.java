package br.com.fiap.payment.infrastructure.outbound.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutboxJpaEntity, UUID> {
    List<PaymentOutboxJpaEntity> findTop10ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus status, Instant now);

    long countByStatus(OutboxStatus status);
}
