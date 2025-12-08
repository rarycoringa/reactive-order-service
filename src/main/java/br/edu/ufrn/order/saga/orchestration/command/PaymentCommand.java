package br.edu.ufrn.order.saga.orchestration.command;

public record PaymentCommand(
    CommandType type,
    String orderId,
    Double amount,
    Integer splitInto,
    String cardNumber
) implements Command {}
