package br.com.fiap.payment.application.usecases.payment;

import br.com.fiap.payment.application.domain.payment.Payment;
import br.com.fiap.payment.application.domain.payment.PaymentAuthorization;
import br.com.fiap.payment.application.ports.inbound.payment.ForProcessingPayment;
import br.com.fiap.payment.application.ports.inbound.payment.PaymentOutput;
import br.com.fiap.payment.application.ports.inbound.payment.ProcessPaymentInput;
import br.com.fiap.payment.application.ports.outbound.gateway.PaymentGatewayPort;
import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultMessage;
import br.com.fiap.payment.application.ports.outbound.messaging.PaymentResultPublisherPort;
import br.com.fiap.payment.application.ports.outbound.repository.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentUseCase implements ForProcessingPayment {
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentResultPublisherPort paymentResultPublisherPort;

    public PaymentUseCase(
            PaymentRepositoryPort paymentRepositoryPort,
            PaymentGatewayPort paymentGatewayPort,
            PaymentResultPublisherPort paymentResultPublisherPort
    ) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentResultPublisherPort = paymentResultPublisherPort;
    }

    @Override
    @Transactional
    public PaymentOutput process(ProcessPaymentInput input) {
        Payment payment = paymentRepositoryPort.findByOrderId(input.orderId())
                .orElseGet(() -> paymentRepositoryPort.save(Payment.start(input.orderId(), input.customerId(), input.amount())));

        PaymentAuthorization authorization = paymentGatewayPort.authorize(payment);
        if (authorization.approved()) {
            payment.approve(authorization.reason());
        } else {
            payment.decline(authorization.reason());
        }

        Payment savedPayment = paymentRepositoryPort.save(payment);
        publishResult(savedPayment);
        return PaymentOutput.from(savedPayment);
    }

    @Override
    @Transactional
    public PaymentOutput fallback(ProcessPaymentInput input, String reason) {
        Payment payment = paymentRepositoryPort.findByOrderId(input.orderId())
                .orElseGet(() -> Payment.start(input.orderId(), input.customerId(), input.amount()));
        payment.fail(reason);

        Payment savedPayment = paymentRepositoryPort.save(payment);
        publishResult(savedPayment);
        return PaymentOutput.from(savedPayment);
    }

    private void publishResult(Payment payment) {
        paymentResultPublisherPort.publish(new PaymentResultMessage(
                payment.getOrderId(),
                payment.isApproved(),
                payment.getReason()
        ));
    }
}
