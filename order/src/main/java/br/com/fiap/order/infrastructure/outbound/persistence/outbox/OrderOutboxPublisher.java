package br.com.fiap.order.infrastructure.outbound.persistence.outbox;

import br.com.fiap.order.infrastructure.outbound.kafka.KafkaPaymentRequestPublisher;
import br.com.fiap.order.infrastructure.outbound.kafka.PaymentRequestedEvent;
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
public class OrderOutboxPublisher {
    private final OrderOutboxJpaRepository outboxJpaRepository;
    private final KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public OrderOutboxPublisher(
            OrderOutboxJpaRepository outboxJpaRepository,
            KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaPaymentRequestPublisher = kafkaPaymentRequestPublisher;
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
    public void publish(OrderOutboxJpaEntity outbox) {
        recordPublishAttempt(outbox);
        try {
            PaymentRequestedEvent event = objectMapper.readValue(outbox.getPayload(), PaymentRequestedEvent.class);
            kafkaPaymentRequestPublisher.send(outbox.getMessageKey(), event).get(10, TimeUnit.SECONDS);
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

    private void recordPublishAttempt(OrderOutboxJpaEntity outbox) {
        Counter.builder("business.outbox.publish.attempts")
                .description("Outbox publish attempts")
                .tag("service", "order")
                .tag("topic", outbox.getTopic())
                .register(meterRegistry)
                .increment();
    }

    private void recordPublishResult(OrderOutboxJpaEntity outbox, String result, String errorType) {
        Counter.builder("business.outbox.publish.results")
                .description("Outbox publish results")
                .tag("service", "order")
                .tag("topic", outbox.getTopic())
                .tag("result", result)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }
}
