package br.com.fiap.order.infrastructure.outbound.persistence.entity;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class OrderItemJpaEntity {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    protected OrderItemJpaEntity() {
    }

    public OrderItemJpaEntity(String productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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
