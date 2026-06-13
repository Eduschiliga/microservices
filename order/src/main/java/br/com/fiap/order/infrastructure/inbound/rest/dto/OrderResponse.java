package br.com.fiap.order.infrastructure.inbound.rest.dto;

import br.com.fiap.order.application.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
