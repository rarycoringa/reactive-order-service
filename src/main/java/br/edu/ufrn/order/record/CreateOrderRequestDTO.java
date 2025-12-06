package br.edu.ufrn.order.record;

public record CreateOrderRequestDTO(
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
) {}
