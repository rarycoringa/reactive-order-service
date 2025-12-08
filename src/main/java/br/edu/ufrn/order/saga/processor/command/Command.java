package br.edu.ufrn.order.saga.processor.command;

public record Command(
    CommandType type,
    String orderId,
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
) {}
