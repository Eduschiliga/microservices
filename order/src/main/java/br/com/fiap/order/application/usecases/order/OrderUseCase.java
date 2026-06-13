package br.com.fiap.order.application.usecases.order;

import br.com.fiap.order.application.domain.order.Order;
import br.com.fiap.order.application.domain.order.OrderItem;
import br.com.fiap.order.application.ports.inbound.order.CreateOrderInput;
import br.com.fiap.order.application.ports.inbound.order.ForCreatingOrder;
import br.com.fiap.order.application.ports.inbound.order.ForGettingOrder;
import br.com.fiap.order.application.ports.inbound.order.ForUpdatingOrderPayment;
import br.com.fiap.order.application.ports.inbound.order.OrderOutput;
import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestMessage;
import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestPublisherPort;
import br.com.fiap.order.application.ports.outbound.repository.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderUseCase implements ForCreatingOrder, ForGettingOrder, ForUpdatingOrderPayment {
    private final OrderRepositoryPort orderRepositoryPort;
    private final PaymentRequestPublisherPort paymentRequestPublisherPort;

    public OrderUseCase(OrderRepositoryPort orderRepositoryPort, PaymentRequestPublisherPort paymentRequestPublisherPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.paymentRequestPublisherPort = paymentRequestPublisherPort;
    }

    @Override
    @Transactional
    public OrderOutput create(CreateOrderInput input) {
        List<OrderItem> items = input.items().stream()
                .map(item -> new OrderItem(item.productId(), item.productName(), item.quantity(), item.unitPrice()))
                .toList();
        Order order = orderRepositoryPort.save(Order.create(input.customerId(), items));

        paymentRequestPublisherPort.publish(new PaymentRequestMessage(
                order.getOrderId(),
                order.getCustomerId(),
                order.getTotalAmount()
        ));

        return OrderOutput.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderOutput getById(UUID orderId) {
        return orderRepositoryPort.findById(orderId)
                .map(OrderOutput::from)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderOutput> list() {
        return orderRepositoryPort.findAll().stream()
                .map(OrderOutput::from)
                .toList();
    }

    @Override
    @Transactional
    public void updatePaymentStatus(UUID orderId, boolean approved) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (approved) {
            order.markPaid();
        } else {
            order.markPaymentFailed();
        }

        orderRepositoryPort.save(order);
    }
}
