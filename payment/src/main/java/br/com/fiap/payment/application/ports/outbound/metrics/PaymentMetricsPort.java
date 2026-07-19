package br.com.fiap.payment.application.ports.outbound.metrics;

import br.com.fiap.payment.application.domain.payment.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentMetricsPort {
    void recordPaymentProcessed(PaymentStatus status, BigDecimal amount);

    void recordPaymentFallback(PaymentStatus status);
}
