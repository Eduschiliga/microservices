package br.com.fiap.payment.infrastructure.outbound.kafka;

import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaPaymentResultPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaPaymentResultPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.payment-results}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PaymentResultMessage message) {
        PaymentResultEvent event = new PaymentResultEvent(message.orderId(), message.approved(), message.reason());
        send(message.orderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> send(String key, PaymentResultEvent event) {
        return kafkaTemplate.send(topic, key, event);
    }

    public String topic() {
        return topic;
    }
}
