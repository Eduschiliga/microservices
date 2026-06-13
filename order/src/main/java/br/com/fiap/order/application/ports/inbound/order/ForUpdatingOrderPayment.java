package br.com.fiap.order.application.ports.inbound.order;

import java.util.UUID;

public interface ForUpdatingOrderPayment {
    void updatePaymentStatus(UUID orderId, boolean approved);
}
