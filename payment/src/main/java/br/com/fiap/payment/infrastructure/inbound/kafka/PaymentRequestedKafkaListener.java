package br.com.fiap.payment.infrastructure.inbound.kafka;

import br.com.fiap.payment.application.ports.inbound.payment.ForProcessingPayment;
import br.com.fiap.payment.application.ports.inbound.payment.ProcessPaymentInput;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestedKafkaListener {
    private final ForProcessingPayment forProcessingPayment;

    public PaymentRequestedKafkaListener(ForProcessingPayment forProcessingPayment) {
        this.forProcessingPayment = forProcessingPayment;
    }

    @RetryableTopic(
            attempts = "${app.kafka.retry.attempts}",
            backOff = @BackOff(
                    delayString = "${app.kafka.retry.delay}",
                    multiplierString = "${app.kafka.retry.multiplier}"
            ),
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${app.kafka.topics.payment-requests}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentRequested(PaymentRequestedEvent event) {
        forProcessingPayment.process(new ProcessPaymentInput(event.orderId(), event.customerId(), event.amount()));
    }

    @DltHandler
    public void onDeadLetter(PaymentRequestedEvent event) {
        forProcessingPayment.fallback(
                new ProcessPaymentInput(event.orderId(), event.customerId(), event.amount()),
                "Payment request sent to dead-letter topic after retries"
        );
    }
}
