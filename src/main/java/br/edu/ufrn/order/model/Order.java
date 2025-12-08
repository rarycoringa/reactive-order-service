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

    private Integer splitInto;
    
    private String cardNumber;
    
    private String address;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant createdAt;

    public Order(String productId, Integer productQuantity, Integer splitInto, String cardNumber, String address) {
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.splitInto = splitInto;
        this.cardNumber = cardNumber;
        this.address = address;
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

    public Integer getSplitInto() {
        return splitInto;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getAddress() {
        return address;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    
}
