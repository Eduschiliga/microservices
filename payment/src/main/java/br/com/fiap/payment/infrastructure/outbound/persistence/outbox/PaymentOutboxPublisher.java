package br.com.fiap.payment.infrastructure.outbound.persistence.outbox;

import br.com.fiap.payment.infrastructure.outbound.kafka.KafkaPaymentResultPublisher;
import br.com.fiap.payment.infrastructure.outbound.kafka.PaymentResultEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class PaymentOutboxPublisher {
    private final PaymentOutboxJpaRepository outboxJpaRepository;
    private final KafkaPaymentResultPublisher kafkaPaymentResultPublisher;
    private final ObjectMapper objectMapper;

    public PaymentOutboxPublisher(
            PaymentOutboxJpaRepository outboxJpaRepository,
            KafkaPaymentResultPublisher kafkaPaymentResultPublisher,
            ObjectMapper objectMapper
    ) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaPaymentResultPublisher = kafkaPaymentResultPublisher;
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
    public void publish(PaymentOutboxJpaEntity outbox) {
        try {
            PaymentResultEvent event = objectMapper.readValue(outbox.getPayload(), PaymentResultEvent.class);
            kafkaPaymentResultPublisher.send(outbox.getMessageKey(), event).get(10, TimeUnit.SECONDS);
            outbox.markPublished();
        } catch (JsonProcessingException exception) {
            outbox.markRetry("Invalid outbox payload: " + exception.getMessage());
        } catch (Exception exception) {
            outbox.markRetry(exception.getMessage());
        }
        outboxJpaRepository.save(outbox);
    }
}
