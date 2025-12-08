package br.edu.ufrn.order.record;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderResponseDTO(
    String id,
    @JsonProperty("product_id") String productId,
    @JsonProperty("product_quantity") Integer productQuantity,
    @JsonProperty("split_into") Integer splitInto,
    @JsonProperty("card_number") String cardNumber,
    String address,
    @JsonProperty("created_at") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt
) {}
