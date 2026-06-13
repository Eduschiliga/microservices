package br.com.fiap.order.infrastructure.outbound.persistence.repository;

import br.com.fiap.order.infrastructure.outbound.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
