package br.com.fiap.payment.infrastructure.observability;

import br.com.fiap.payment.application.domain.payment.PaymentStatus;
import br.com.fiap.payment.application.ports.outbound.metrics.PaymentMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentBusinessMetrics implements PaymentMetricsPort {
    private final MeterRegistry meterRegistry;

    public PaymentBusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordPaymentProcessed(PaymentStatus status, BigDecimal amount) {
        Counter.builder("business.payments.processed")
                .description("Payments processed by status")
                .tag("status", status.name().toLowerCase())
                .register(meterRegistry)
                .increment();
        DistributionSummary.builder("business.payments.amount")
                .description("Payment amount by status")
                .tag("status", status.name().toLowerCase())
                .register(meterRegistry)
                .record(amount.doubleValue());
    }

    @Override
    public void recordPaymentFallback(PaymentStatus status) {
        Counter.builder("business.payments.fallback")
                .description("Payments completed through fallback")
                .tag("status", status.name().toLowerCase())
                .register(meterRegistry)
                .increment();
    }
}
