package br.com.fiap.payment.application.ports.inbound.payment;

public interface ForProcessingPayment {
    PaymentOutput process(ProcessPaymentInput input);

    PaymentOutput fallback(ProcessPaymentInput input, String reason);
}
