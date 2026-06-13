package br.com.fiap.order.infrastructure.outbound.persistence.entity;

import br.com.fiap.order.application.domain.order.OrderStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    protected OrderJpaEntity() {
    }

    public OrderJpaEntity(UUID orderId, UUID customerId, BigDecimal totalAmount, OrderStatus status, Instant createdAt, Instant updatedAt, List<OrderItemJpaEntity> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = new ArrayList<>(items);
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItemJpaEntity> getItems() {
        return items;
    }
}
