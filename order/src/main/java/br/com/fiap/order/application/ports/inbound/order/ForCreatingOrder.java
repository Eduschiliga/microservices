package br.com.fiap.order.application.ports.inbound.order;

public interface ForCreatingOrder {
    OrderOutput create(CreateOrderInput input);
}
