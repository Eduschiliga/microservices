package br.com.fiap.payment.application.domain.payment;

public record PaymentAuthorization(boolean approved, String reason) {}
