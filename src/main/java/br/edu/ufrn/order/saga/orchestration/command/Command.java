package br.edu.ufrn.order.saga.orchestration.command;

public record Command(
    CommandType type,
    String orderId,
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
) {}
