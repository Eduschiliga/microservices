package br.com.fiap.order.infrastructure.observability;

import br.com.fiap.order.application.domain.order.OrderStatus;
import br.com.fiap.order.infrastructure.outbound.persistence.outbox.OrderOutboxJpaRepository;
import br.com.fiap.order.infrastructure.outbound.persistence.outbox.OutboxStatus;
import br.com.fiap.order.infrastructure.outbound.persistence.repository.OrderJpaRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderBusinessMetricsConfiguration {
    public OrderBusinessMetricsConfiguration(
            MeterRegistry meterRegistry,
            OrderJpaRepository orderJpaRepository,
            OrderOutboxJpaRepository outboxJpaRepository
    ) {
        for (OrderStatus status : OrderStatus.values()) {
            Gauge.builder("business.orders.current", orderJpaRepository, repository -> repository.countByStatus(status))
                    .description("Current orders by status")
                    .tag("status", status.name().toLowerCase())
                    .register(meterRegistry);
        }

        for (OutboxStatus status : OutboxStatus.values()) {
            Gauge.builder("business.outbox.events.current", outboxJpaRepository, repository -> repository.countByStatus(status))
                    .description("Current outbox events by status")
                    .tag("service", "order")
                    .tag("status", status.name().toLowerCase())
                    .register(meterRegistry);
        }
    }
}
