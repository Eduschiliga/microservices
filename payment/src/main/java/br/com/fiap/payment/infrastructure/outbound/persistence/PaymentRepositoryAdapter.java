package br.com.fiap.payment.infrastructure.outbound.persistence;

import br.com.fiap.payment.application.domain.payment.Payment;
import br.com.fiap.payment.application.ports.outbound.repository.PaymentRepositoryPort;
import br.com.fiap.payment.infrastructure.outbound.persistence.entity.PaymentJpaEntity;
import br.com.fiap.payment.infrastructure.outbound.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return toDomain(paymentJpaRepository.save(toEntity(payment)));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId).map(this::toDomain);
    }

    private PaymentJpaEntity toEntity(Payment payment) {
        return new PaymentJpaEntity(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(
                entity.getPaymentId(),
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
