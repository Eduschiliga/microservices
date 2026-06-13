package br.com.fiap.payment.infrastructure.outbound.gateway;

import br.com.fiap.payment.application.domain.payment.Payment;
import br.com.fiap.payment.application.domain.payment.PaymentAuthorization;
import br.com.fiap.payment.application.ports.outbound.gateway.PaymentGatewayPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    @Retry(name = "payment-gateway")
    @CircuitBreaker(name = "payment-gateway", fallbackMethod = "authorizeFallback")
    public PaymentAuthorization authorize(Payment payment) {
        if (payment.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            return new PaymentAuthorization(false, "Amount exceeds simulated approval limit");
        }
        return new PaymentAuthorization(true, "Payment approved by simulated gateway");
    }

    private PaymentAuthorization authorizeFallback(Payment payment, Throwable throwable) {
        return new PaymentAuthorization(false, "Payment gateway fallback: " + throwable.getMessage());
    }
}
