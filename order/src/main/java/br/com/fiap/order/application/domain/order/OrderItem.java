package br.com.fiap.order.application.domain.order;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product id is required");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
