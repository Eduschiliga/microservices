package br.com.fiap.payment.application.ports.outbound.repository;

import br.com.fiap.payment.application.domain.payment.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(UUID orderId);
}
