package br.com.fiap.order.application.ports.inbound.order;

import java.util.List;
import java.util.UUID;

public interface ForGettingOrder {
    OrderOutput getById(UUID orderId);

    List<OrderOutput> list();
}
