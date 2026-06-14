package br.com.fiap.order.infrastructure.outbound.persistence.outbox;

import br.com.fiap.order.infrastructure.outbound.kafka.KafkaPaymentRequestPublisher;
import br.com.fiap.order.infrastructure.outbound.kafka.PaymentRequestedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class OrderOutboxPublisher {
    private final OrderOutboxJpaRepository outboxJpaRepository;
    private final KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher;
    private final ObjectMapper objectMapper;

    public OrderOutboxPublisher(
            OrderOutboxJpaRepository outboxJpaRepository,
            KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher,
            ObjectMapper objectMapper
    ) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaPaymentRequestPublisher = kafkaPaymentRequestPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(
            fixedDelayString = "${app.outbox.poll-interval-ms:1000}",
            initialDelayString = "${app.outbox.initial-delay-ms:1000}"
    )
    public void publishPending() {
        outboxJpaRepository
                .findTop10ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus.PENDING, Instant.now())
                .forEach(this::publish);
    }

    @Transactional
    public void publish(OrderOutboxJpaEntity outbox) {
        try {
            PaymentRequestedEvent event = objectMapper.readValue(outbox.getPayload(), PaymentRequestedEvent.class);
            kafkaPaymentRequestPublisher.send(outbox.getMessageKey(), event).get(10, TimeUnit.SECONDS);
            outbox.markPublished();
        } catch (JsonProcessingException exception) {
            outbox.markRetry("Invalid outbox payload: " + exception.getMessage());
        } catch (Exception exception) {
            outbox.markRetry(exception.getMessage());
        }
        outboxJpaRepository.save(outbox);
    }
}
