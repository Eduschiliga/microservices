package br.com.fiap.order.application.ports.inbound.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderInput(UUID customerId, List<CreateOrderItemInput> items) {
    public record CreateOrderItemInput(String productId, String productName, int quantity, BigDecimal unitPrice) {}
}
