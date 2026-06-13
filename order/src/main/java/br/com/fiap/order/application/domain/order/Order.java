package br.com.fiap.order.application.domain.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {
    private final UUID orderId;
    private final UUID customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(UUID orderId, UUID customerId, List<OrderItem> items, OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.orderId = Objects.requireNonNull(orderId);
        this.customerId = Objects.requireNonNull(customerId);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.items = List.copyOf(items);
        this.totalAmount = this.items.stream()
                .map(OrderItem::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Order create(UUID customerId, List<OrderItem> items) {
        Instant now = Instant.now();
        return new Order(UUID.randomUUID(), customerId, items, OrderStatus.PENDING_PAYMENT, now, now);
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void markPaymentFailed() {
        this.status = OrderStatus.PAYMENT_FAILED;
        this.updatedAt = Instant.now();
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
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
}
