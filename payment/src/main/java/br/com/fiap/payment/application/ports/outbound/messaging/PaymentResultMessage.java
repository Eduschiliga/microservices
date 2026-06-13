package br.com.fiap.payment.application.ports.outbound.messaging;

import java.util.UUID;

public record PaymentResultMessage(UUID orderId, boolean approved, String reason) {}
