package br.com.fiap.order.application.ports.outbound.metrics;

import br.com.fiap.order.application.domain.order.OrderStatus;

import java.math.BigDecimal;

public interface OrderMetricsPort {
    void recordOrderCreated(OrderStatus status, BigDecimal totalAmount);

    void recordPaymentStatusUpdated(OrderStatus status);
}
