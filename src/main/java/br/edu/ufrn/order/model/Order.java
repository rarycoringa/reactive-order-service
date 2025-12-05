package br.edu.ufrn.order.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String productId;

    private String cardNumber;

    private String address;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant createdAt;

    public Order() {
        
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    
}
