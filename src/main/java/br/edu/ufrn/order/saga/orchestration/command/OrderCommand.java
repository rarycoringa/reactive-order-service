package br.edu.ufrn.order.saga.orchestration.command;

public record OrderCommand(
    CommandType type,
    String orderId,
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
) implements Command {}
