package br.com.fiap.payment.application.ports.outbound.messaging;

public interface PaymentResultPublisherPort {
    void publish(PaymentResultMessage message);
}
