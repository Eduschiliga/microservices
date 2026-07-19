package br.com.fiap.payment.infrastructure.outbound.persistence.outbox;

import br.com.fiap.payment.infrastructure.outbound.kafka.KafkaPaymentResultPublisher;
import br.com.fiap.payment.infrastructure.outbound.kafka.PaymentResultEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    public PaymentOutboxPublisher(
            PaymentOutboxJpaRepository outboxJpaRepository,
            KafkaPaymentResultPublisher kafkaPaymentResultPublisher,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaPaymentResultPublisher = kafkaPaymentResultPublisher;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
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
        recordPublishAttempt(outbox);
        try {
            PaymentResultEvent event = objectMapper.readValue(outbox.getPayload(), PaymentResultEvent.class);
            kafkaPaymentResultPublisher.send(outbox.getMessageKey(), event).get(10, TimeUnit.SECONDS);
            outbox.markPublished();
            recordPublishResult(outbox, "published", "none");
        } catch (JsonProcessingException exception) {
            outbox.markRetry("Invalid outbox payload: " + exception.getMessage());
            recordPublishResult(outbox, "retry", "payload");
        } catch (Exception exception) {
            outbox.markRetry(exception.getMessage());
            recordPublishResult(outbox, "retry", "exception");
        }
        outboxJpaRepository.save(outbox);
    }

    private void recordPublishAttempt(PaymentOutboxJpaEntity outbox) {
        Counter.builder("business.outbox.publish.attempts")
                .description("Outbox publish attempts")
                .tag("service", "payment")
                .tag("topic", outbox.getTopic())
                .register(meterRegistry)
                .increment();
    }

    private void recordPublishResult(PaymentOutboxJpaEntity outbox, String result, String errorType) {
        Counter.builder("business.outbox.publish.results")
                .description("Outbox publish results")
                .tag("service", "payment")
                .tag("topic", outbox.getTopic())
                .tag("result", result)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }
}
