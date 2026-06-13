package br.com.fiap.payment.application.ports.outbound.gateway;

import br.com.fiap.payment.application.domain.payment.Payment;
import br.com.fiap.payment.application.domain.payment.PaymentAuthorization;

public interface PaymentGatewayPort {
    PaymentAuthorization authorize(Payment payment);
}
