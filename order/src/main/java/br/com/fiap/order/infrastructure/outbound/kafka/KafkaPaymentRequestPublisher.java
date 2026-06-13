package br.com.fiap.order.infrastructure.outbound.kafka;

import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestMessage;
import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentRequestPublisher implements PaymentRequestPublisherPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaPaymentRequestPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.payment-requests}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(PaymentRequestMessage message) {
        PaymentRequestedEvent event = new PaymentRequestedEvent(message.orderId(), message.customerId(), message.amount());
        kafkaTemplate.send(topic, message.orderId().toString(), event);
    }
}
