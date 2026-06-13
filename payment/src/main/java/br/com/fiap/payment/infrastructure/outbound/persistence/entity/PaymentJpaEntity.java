package br.com.fiap.payment.infrastructure.outbound.persistence.entity;

import br.com.fiap.payment.application.domain.payment.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {
    @Id
    private UUID paymentId;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String reason;
    private Instant createdAt;
    private Instant updatedAt;

    protected PaymentJpaEntity() {
    }

    public PaymentJpaEntity(UUID paymentId, UUID orderId, UUID customerId, BigDecimal amount, PaymentStatus status, String reason, Instant createdAt, Instant updatedAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
