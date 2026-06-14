package br.com.fiap.payment.infrastructure.outbound.persistence.outbox;

import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultMessage;
import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultPublisherPort;
import br.com.fiap.payment.infrastructure.outbound.kafka.KafkaPaymentResultPublisher;
import br.com.fiap.payment.infrastructure.outbound.kafka.PaymentResultEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxPaymentResultPublisher implements PaymentResultPublisherPort {
    private static final String EVENT_TYPE = "PaymentResult";

    private final PaymentOutboxJpaRepository outboxJpaRepository;
    private final KafkaPaymentResultPublisher kafkaPaymentResultPublisher;
    private final ObjectMapper objectMapper;

    public OutboxPaymentResultPublisher(
            PaymentOutboxJpaRepository outboxJpaRepository,
            KafkaPaymentResultPublisher kafkaPaymentResultPublisher,
            ObjectMapper objectMapper
    ) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaPaymentResultPublisher = kafkaPaymentResultPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(PaymentResultMessage message) {
        PaymentResultEvent event = new PaymentResultEvent(message.orderId(), message.approved(), message.reason());
        outboxJpaRepository.save(new PaymentOutboxJpaEntity(
                message.orderId(),
                EVENT_TYPE,
                kafkaPaymentResultPublisher.topic(),
                message.orderId().toString(),
                toJson(event)
        ));
    }

    private String toJson(PaymentResultEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize payment result outbox event", exception);
        }
    }
}
