package br.edu.ufrn.order.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Positive;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String productId;

    @Positive(message = "Product Quantity should be higher than zero.")
    private Integer productQuantity;

    private String paymentChargeId;

    private String paymentRefundId;

    private String shippingId;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant createdAt;

    public Order(String productId, Integer productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public String getPaymentChargeId() {
        return paymentChargeId;
    }

    public void setPaymentChargeId(String paymentChargeId) {
        this.paymentChargeId = paymentChargeId;
    }

    public String getPaymentRefundId() {
        return paymentRefundId;
    }

    public void setPaymentRefundId(String paymentRefundId) {
        this.paymentRefundId = paymentRefundId;
    }

    public String getShippingId() {
        return shippingId;
    }

    public void setShippingId(String shippingId) {
        this.shippingId = shippingId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    
}
