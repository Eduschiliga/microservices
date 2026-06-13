package br.com.fiap.order.infrastructure.outbound.persistence;

import br.com.fiap.order.application.domain.order.Order;
import br.com.fiap.order.application.domain.order.OrderItem;
import br.com.fiap.order.application.ports.outbound.repository.OrderRepositoryPort;
import br.com.fiap.order.infrastructure.outbound.persistence.entity.OrderItemJpaEntity;
import br.com.fiap.order.infrastructure.outbound.persistence.entity.OrderJpaEntity;
import br.com.fiap.order.infrastructure.outbound.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        return toDomain(orderJpaRepository.save(toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId).map(this::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private OrderJpaEntity toEntity(Order order) {
        return new OrderJpaEntity(
                order.getOrderId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderItemJpaEntity(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                        .toList()
        );
    }

    private Order toDomain(OrderJpaEntity entity) {
        return new Order(
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getItems().stream()
                        .map(item -> new OrderItem(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                        .toList(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
