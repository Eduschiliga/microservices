package br.com.fiap.payment.application.domain.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {
    private final UUID paymentId;
    private final UUID orderId;
    private final UUID customerId;
    private final BigDecimal amount;
    private PaymentStatus status;
    private String reason;
    private final Instant createdAt;
    private Instant updatedAt;

    public Payment(UUID paymentId, UUID orderId, UUID customerId, BigDecimal amount, PaymentStatus status, String reason, Instant createdAt, Instant updatedAt) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.orderId = Objects.requireNonNull(orderId);
        this.customerId = Objects.requireNonNull(customerId);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        this.amount = amount;
        this.status = Objects.requireNonNull(status);
        this.reason = reason;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Payment start(UUID orderId, UUID customerId, BigDecimal amount) {
        Instant now = Instant.now();
        return new Payment(UUID.randomUUID(), orderId, customerId, amount, PaymentStatus.PROCESSING, null, now, now);
    }

    public void approve(String reason) {
        this.status = PaymentStatus.APPROVED;
        this.reason = reason;
        this.updatedAt = Instant.now();
    }

    public void decline(String reason) {
        this.status = PaymentStatus.DECLINED;
        this.reason = reason;
        this.updatedAt = Instant.now();
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.reason = reason;
        this.updatedAt = Instant.now();
    }

    public boolean isApproved() {
        return PaymentStatus.APPROVED.equals(status);
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
