package br.edu.ufrn.order.saga.orchestration.event;

public record PaymentEvent(
    EventType type,
    String orderId,
    String paymentChargeId,
    String paymentRefundId,
    Double amount,
    Integer splitInto,
    String cardNumber
) implements Event {}
