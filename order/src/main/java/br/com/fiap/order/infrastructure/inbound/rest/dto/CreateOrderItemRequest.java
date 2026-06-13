package br.com.fiap.order.infrastructure.inbound.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotBlank String productId,
        @NotBlank String productName,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.01") BigDecimal unitPrice
) {}
