package br.com.fiap.order.infrastructure.inbound.rest.dto;

import java.math.BigDecimal;

public record OrderItemResponse(String productId, String productName, int quantity, BigDecimal unitPrice) {}
