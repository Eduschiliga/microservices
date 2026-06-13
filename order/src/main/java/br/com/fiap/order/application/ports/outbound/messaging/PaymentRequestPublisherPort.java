package br.com.fiap.order.application.ports.outbound.messaging;

public interface PaymentRequestPublisherPort {
    void publish(PaymentRequestMessage message);
}
