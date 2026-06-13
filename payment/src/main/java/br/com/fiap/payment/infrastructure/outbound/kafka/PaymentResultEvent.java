package br.com.fiap.payment.infrastructure.outbound.kafka;

import java.util.UUID;

public record PaymentResultEvent(UUID orderId, boolean approved, String reason) {}
