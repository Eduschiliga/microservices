package br.com.fiap.order.infrastructure.inbound.rest;

import br.com.fiap.order.application.ports.inbound.order.CreateOrderInput;
import br.com.fiap.order.application.ports.inbound.order.ForCreatingOrder;
import br.com.fiap.order.application.ports.inbound.order.ForGettingOrder;
import br.com.fiap.order.application.ports.inbound.order.OrderOutput;
import br.com.fiap.order.infrastructure.inbound.rest.dto.CreateOrderRequest;
import br.com.fiap.order.infrastructure.inbound.rest.dto.OrderItemResponse;
import br.com.fiap.order.infrastructure.inbound.rest.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final ForCreatingOrder forCreatingOrder;
    private final ForGettingOrder forGettingOrder;

    public OrderController(ForCreatingOrder forCreatingOrder, ForGettingOrder forGettingOrder) {
        this.forCreatingOrder = forCreatingOrder;
        this.forGettingOrder = forGettingOrder;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderOutput output = forCreatingOrder.create(new CreateOrderInput(
                request.customerId(),
                request.items().stream()
                        .map(item -> new CreateOrderInput.CreateOrderItemInput(
                                item.productId(),
                                item.productName(),
                                item.quantity(),
                                item.unitPrice()
                        ))
                        .toList()
        ));

        return ResponseEntity.created(URI.create("/api/v1/orders/" + output.orderId()))
                .body(toResponse(output));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(toResponse(forGettingOrder.getById(orderId)));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list() {
        return ResponseEntity.ok(forGettingOrder.list().stream()
                .map(this::toResponse)
                .toList());
    }

    private OrderResponse toResponse(OrderOutput output) {
        return new OrderResponse(
                output.orderId(),
                output.customerId(),
                output.items().stream()
                        .map(item -> new OrderItemResponse(item.productId(), item.productName(), item.quantity(), item.unitPrice()))
                        .toList(),
                output.totalAmount(),
                output.status(),
                output.createdAt(),
                output.updatedAt()
        );
    }
}
