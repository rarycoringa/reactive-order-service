package br.edu.ufrn.order.saga.orchestration.command;

public record ShippingCommand(
    CommandType type,
    String orderId,
    String address
) implements Command {}
