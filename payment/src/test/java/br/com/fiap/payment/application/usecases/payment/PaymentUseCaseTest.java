package br.com.fiap.payment.application.usecases.payment;

import br.com.fiap.payment.application.domain.payment.Payment;
import br.com.fiap.payment.application.domain.payment.PaymentAuthorization;
import br.com.fiap.payment.application.domain.payment.PaymentStatus;
import br.com.fiap.payment.application.ports.inbound.payment.PaymentOutput;
import br.com.fiap.payment.application.ports.inbound.payment.ProcessPaymentInput;
import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultMessage;
import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultPublisherPort;
import br.com.fiap.payment.application.ports.outbound.metrics.PaymentMetricsPort;
import br.com.fiap.payment.application.ports.outbound.repository.PaymentRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentUseCaseTest {

    @Test
    void shouldApprovePaymentAndPublishResult() {
        InMemoryPaymentRepository repository = new InMemoryPaymentRepository();
        RecordingPaymentResultPublisher publisher = new RecordingPaymentResultPublisher();
        RecordingPaymentMetrics metrics = new RecordingPaymentMetrics();
        PaymentUseCase useCase = new PaymentUseCase(
                repository,
                payment -> new PaymentAuthorization(true, "approved"),
                publisher,
                metrics
        );

        PaymentOutput output = useCase.process(new ProcessPaymentInput(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN));

        assertThat(output.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(publisher.messages).hasSize(1);
        assertThat(publisher.messages.getFirst().approved()).isTrue();
        assertThat(metrics.processedStatuses).containsExactly(PaymentStatus.APPROVED);
        assertThat(metrics.processedAmounts).containsExactly(BigDecimal.TEN);
    }

    @Test
    void shouldFallbackPaymentAndPublishFailedResult() {
        InMemoryPaymentRepository repository = new InMemoryPaymentRepository();
        RecordingPaymentResultPublisher publisher = new RecordingPaymentResultPublisher();
        RecordingPaymentMetrics metrics = new RecordingPaymentMetrics();
        PaymentUseCase useCase = new PaymentUseCase(
                repository,
                payment -> new PaymentAuthorization(true, "approved"),
                publisher,
                metrics
        );

        PaymentOutput output = useCase.fallback(new ProcessPaymentInput(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN), "dlt");

        assertThat(output.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(publisher.messages).hasSize(1);
        assertThat(publisher.messages.getFirst().approved()).isFalse();
        assertThat(metrics.processedStatuses).containsExactly(PaymentStatus.FAILED);
        assertThat(metrics.processedAmounts).containsExactly(BigDecimal.TEN);
        assertThat(metrics.fallbackStatuses).containsExactly(PaymentStatus.FAILED);
    }

    private static class InMemoryPaymentRepository implements PaymentRepositoryPort {
        private final List<Payment> payments = new ArrayList<>();

        @Override
        public Payment save(Payment payment) {
            payments.removeIf(existing -> existing.getPaymentId().equals(payment.getPaymentId()));
            payments.add(payment);
            return payment;
        }

        @Override
        public Optional<Payment> findByOrderId(UUID orderId) {
            return payments.stream().filter(payment -> payment.getOrderId().equals(orderId)).findFirst();
        }
    }

    private static class RecordingPaymentResultPublisher implements PaymentResultPublisherPort {
        private final List<PaymentResultMessage> messages = new ArrayList<>();

        @Override
        public void publish(PaymentResultMessage message) {
            messages.add(message);
        }
    }

    private static class RecordingPaymentMetrics implements PaymentMetricsPort {
        private final List<PaymentStatus> processedStatuses = new ArrayList<>();
        private final List<BigDecimal> processedAmounts = new ArrayList<>();
        private final List<PaymentStatus> fallbackStatuses = new ArrayList<>();

        @Override
        public void recordPaymentProcessed(PaymentStatus status, BigDecimal amount) {
            processedStatuses.add(status);
            processedAmounts.add(amount);
        }

        @Override
        public void recordPaymentFallback(PaymentStatus status) {
            fallbackStatuses.add(status);
        }
    }
}
