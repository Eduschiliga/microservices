package br.com.fiap.order.application.ports.outbound.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestMessage(UUID orderId, UUID customerId, BigDecimal amount) {}
