package br.com.fiap.payment.infrastructure.outbound.kafka;

import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultMessage;
import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentResultPublisher implements PaymentResultPublisherPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaPaymentResultPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.payment-results}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(PaymentResultMessage message) {
        PaymentResultEvent event = new PaymentResultEvent(message.orderId(), message.approved(), message.reason());
        kafkaTemplate.send(topic, message.orderId().toString(), event);
    }
}
