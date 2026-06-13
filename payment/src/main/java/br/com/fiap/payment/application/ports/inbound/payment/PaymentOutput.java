package br.com.fiap.payment.application.ports.inbound.payment;

import br.com.fiap.payment.application.domain.payment.Payment;
import br.com.fiap.payment.application.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentOutput(
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        PaymentStatus status,
        String reason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentOutput from(Payment payment) {
        return new PaymentOutput(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
