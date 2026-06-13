package br.com.fiap.payment.application.ports.inbound.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentInput(UUID orderId, UUID customerId, BigDecimal amount) {}
