package br.com.fiap.payment.infrastructure.inbound.kafka;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestedEvent(UUID orderId, UUID customerId, BigDecimal amount) {}
