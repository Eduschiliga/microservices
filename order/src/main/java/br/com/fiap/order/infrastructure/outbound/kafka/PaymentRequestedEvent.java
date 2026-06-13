package br.com.fiap.order.infrastructure.outbound.kafka;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestedEvent(UUID orderId, UUID customerId, BigDecimal amount) {}
