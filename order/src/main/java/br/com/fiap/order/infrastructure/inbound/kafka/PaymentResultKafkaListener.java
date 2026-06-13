package br.com.fiap.order.infrastructure.inbound.kafka;

import br.com.fiap.order.application.ports.inbound.order.ForUpdatingOrderPayment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultKafkaListener {
    private final ForUpdatingOrderPayment forUpdatingOrderPayment;

    public PaymentResultKafkaListener(ForUpdatingOrderPayment forUpdatingOrderPayment) {
        this.forUpdatingOrderPayment = forUpdatingOrderPayment;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-results}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentResult(PaymentResultEvent event) {
        forUpdatingOrderPayment.updatePaymentStatus(event.orderId(), event.approved());
    }
}
