package br.com.fiap.order.application.ports.inbound.order;

import br.com.fiap.order.application.domain.order.Order;
import br.com.fiap.order.application.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderOutput(
        UUID orderId,
        UUID customerId,
        List<OrderItemOutput> items,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderOutput from(Order order) {
        return new OrderOutput(
                order.getOrderId(),
                order.getCustomerId(),
                order.getItems().stream()
                        .map(item -> new OrderItemOutput(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                        .toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public record OrderItemOutput(String productId, String productName, int quantity, BigDecimal unitPrice) {}
}
