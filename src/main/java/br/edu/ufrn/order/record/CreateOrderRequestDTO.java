package br.edu.ufrn.order.record;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateOrderRequestDTO(
    @JsonProperty("product_id") String productId,
    @JsonProperty("product_quantity") Integer productQuantity,
    @JsonProperty("split_into") Integer splitInto,
    @JsonProperty("card_number") String cardNumber,
    String address
) {}
