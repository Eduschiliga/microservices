package br.com.fiap.order.infrastructure.outbound.kafka;

import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaPaymentRequestPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaPaymentRequestPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.payment-requests}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PaymentRequestMessage message) {
        PaymentRequestedEvent event = new PaymentRequestedEvent(message.orderId(), message.customerId(), message.amount());
        send(message.orderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> send(String key, PaymentRequestedEvent event) {
        return kafkaTemplate.send(topic, key, event);
    }

    public String topic() {
        return topic;
    }
}
