package br.com.fiap.order.infrastructure.observability;

import br.com.fiap.order.application.domain.order.OrderStatus;
import br.com.fiap.order.application.ports.outbound.metrics.OrderMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderBusinessMetrics implements OrderMetricsPort {
    private final MeterRegistry meterRegistry;
    private final DistributionSummary orderAmountSummary;

    public OrderBusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderAmountSummary = DistributionSummary.builder("business.orders.amount")
                .description("Total amount of created orders")
                .register(meterRegistry);
    }

    @Override
    public void recordOrderCreated(OrderStatus status, BigDecimal totalAmount) {
        Counter.builder("business.orders.created.events")
                .description("Orders created by status")
                .tag("status", status.name().toLowerCase())
                .register(meterRegistry)
                .increment();
        orderAmountSummary.record(totalAmount.doubleValue());
    }

    @Override
    public void recordPaymentStatusUpdated(OrderStatus status) {
        Counter.builder("business.orders.payment.status.updated")
                .description("Order payment status updates")
                .tag("status", status.name().toLowerCase())
                .register(meterRegistry)
                .increment();
    }
}
