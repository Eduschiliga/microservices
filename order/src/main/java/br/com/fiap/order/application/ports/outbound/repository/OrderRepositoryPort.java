package br.com.fiap.order.application.ports.outbound.repository;

import br.com.fiap.order.application.domain.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findAll();
}
