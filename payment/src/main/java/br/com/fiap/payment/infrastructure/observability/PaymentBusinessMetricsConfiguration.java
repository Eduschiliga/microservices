package br.com.fiap.payment.infrastructure.observability;

import br.com.fiap.payment.application.domain.payment.PaymentStatus;
import br.com.fiap.payment.infrastructure.outbound.persistence.outbox.OutboxStatus;
import br.com.fiap.payment.infrastructure.outbound.persistence.outbox.PaymentOutboxJpaRepository;
import br.com.fiap.payment.infrastructure.outbound.persistence.repository.PaymentJpaRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentBusinessMetricsConfiguration {
    public PaymentBusinessMetricsConfiguration(
            MeterRegistry meterRegistry,
            PaymentJpaRepository paymentJpaRepository,
            PaymentOutboxJpaRepository outboxJpaRepository
    ) {
        for (PaymentStatus status : PaymentStatus.values()) {
            Gauge.builder("business.payments.current", paymentJpaRepository, repository -> repository.countByStatus(status))
                    .description("Current payments by status")
                    .tag("status", status.name().toLowerCase())
                    .register(meterRegistry);
        }

        for (OutboxStatus status : OutboxStatus.values()) {
            Gauge.builder("business.outbox.events.current", outboxJpaRepository, repository -> repository.countByStatus(status))
                    .description("Current outbox events by status")
                    .tag("service", "payment")
                    .tag("status", status.name().toLowerCase())
                    .register(meterRegistry);
        }
    }
}
