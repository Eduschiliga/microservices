package br.com.fiap.order.application.usecases.order;

import br.com.fiap.order.application.domain.order.Order;
import br.com.fiap.order.application.domain.order.OrderStatus;
import br.com.fiap.order.application.ports.inbound.order.CreateOrderInput;
import br.com.fiap.order.application.ports.inbound.order.OrderOutput;
import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestMessage;
import br.com.fiap.order.application.ports.outbound.messaging.PaymentRequestPublisherPort;
import br.com.fiap.order.application.ports.outbound.metrics.OrderMetricsPort;
import br.com.fiap.order.application.ports.outbound.repository.OrderRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderUseCaseTest {

    @Test
    void shouldCreateOrderAndPublishPaymentRequest() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        RecordingPaymentPublisher publisher = new RecordingPaymentPublisher();
        RecordingOrderMetrics metrics = new RecordingOrderMetrics();
        OrderUseCase useCase = new OrderUseCase(repository, publisher, metrics);

        OrderOutput output = useCase.create(new CreateOrderInput(
                UUID.randomUUID(),
                List.of(new CreateOrderInput.CreateOrderItemInput("p1", "Pizza", 2, BigDecimal.TEN))
        ));

        assertThat(output.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(output.totalAmount()).isEqualByComparingTo("20");
        assertThat(repository.findById(output.orderId())).isPresent();
        assertThat(publisher.messages).hasSize(1);
        assertThat(publisher.messages.getFirst().orderId()).isEqualTo(output.orderId());
        assertThat(metrics.createdStatuses).containsExactly(OrderStatus.PENDING_PAYMENT);
        assertThat(metrics.createdAmounts).containsExactly(BigDecimal.valueOf(20));
    }

    @Test
    void shouldUpdateOrderStatusFromPaymentResult() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        RecordingOrderMetrics metrics = new RecordingOrderMetrics();
        OrderUseCase useCase = new OrderUseCase(repository, message -> {}, metrics);
        OrderOutput output = useCase.create(new CreateOrderInput(
                UUID.randomUUID(),
                List.of(new CreateOrderInput.CreateOrderItemInput("p1", "Pizza", 1, BigDecimal.TEN))
        ));

        useCase.updatePaymentStatus(output.orderId(), true);

        assertThat(repository.findById(output.orderId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(metrics.updatedStatuses).containsExactly(OrderStatus.PAID);
    }

    private static class InMemoryOrderRepository implements OrderRepositoryPort {
        private final List<Order> orders = new ArrayList<>();

        @Override
        public Order save(Order order) {
            orders.removeIf(existing -> existing.getOrderId().equals(order.getOrderId()));
            orders.add(order);
            return order;
        }

        @Override
        public Optional<Order> findById(UUID orderId) {
            return orders.stream().filter(order -> order.getOrderId().equals(orderId)).findFirst();
        }

        @Override
        public List<Order> findAll() {
            return List.copyOf(orders);
        }
    }

    private static class RecordingPaymentPublisher implements PaymentRequestPublisherPort {
        private final List<PaymentRequestMessage> messages = new ArrayList<>();

        @Override
        public void publish(PaymentRequestMessage message) {
            messages.add(message);
        }
    }

    private static class RecordingOrderMetrics implements OrderMetricsPort {
        private final List<OrderStatus> createdStatuses = new ArrayList<>();
        private final List<BigDecimal> createdAmounts = new ArrayList<>();
        private final List<OrderStatus> updatedStatuses = new ArrayList<>();

        @Override
        public void recordOrderCreated(OrderStatus status, BigDecimal totalAmount) {
            createdStatuses.add(status);
            createdAmounts.add(totalAmount);
        }

        @Override
        public void recordPaymentStatusUpdated(OrderStatus status) {
            updatedStatuses.add(status);
        }
    }
}
