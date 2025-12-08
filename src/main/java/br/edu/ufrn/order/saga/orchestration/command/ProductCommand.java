package br.edu.ufrn.order.saga.orchestration.command;

public record ProductCommand(
    CommandType type,
    String orderId,
    String productId,
    Integer productQuantity
) implements Command {}
