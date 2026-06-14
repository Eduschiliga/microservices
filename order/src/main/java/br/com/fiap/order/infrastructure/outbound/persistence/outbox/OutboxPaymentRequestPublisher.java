package br.com.fiap.order.infrastructure.outbound.persistence.outbox;

import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestMessage;
import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestPublisherPort;
import br.com.fiap.order.infrastructure.outbound.kafka.KafkaPaymentRequestPublisher;
import br.com.fiap.order.infrastructure.outbound.kafka.PaymentRequestedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxPaymentRequestPublisher implements PaymentRequestPublisherPort {
    private static final String EVENT_TYPE = "PaymentRequested";

    private final OrderOutboxJpaRepository outboxJpaRepository;
    private final KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher;
    private final ObjectMapper objectMapper;

    public OutboxPaymentRequestPublisher(
            OrderOutboxJpaRepository outboxJpaRepository,
            KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher,
            ObjectMapper objectMapper
    ) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaPaymentRequestPublisher = kafkaPaymentRequestPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(PaymentRequestMessage message) {
        PaymentRequestedEvent event = new PaymentRequestedEvent(message.orderId(), message.customerId(), message.amount());
        outboxJpaRepository.save(new OrderOutboxJpaEntity(
                message.orderId(),
                EVENT_TYPE,
                kafkaPaymentRequestPublisher.topic(),
                message.orderId().toString(),
                toJson(event)
        ));
    }

    private String toJson(PaymentRequestedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize payment request outbox event", exception);
        }
    }
}
