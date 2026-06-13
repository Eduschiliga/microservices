package br.com.fiap.payment.infrastructure.outbound.persistence.repository;

import br.com.fiap.payment.infrastructure.outbound.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);
}
