package br.com.fiap.order.infrastructure.inbound.kafka;

import java.util.UUID;

public record PaymentResultEvent(UUID orderId, boolean approved, String reason) {}
