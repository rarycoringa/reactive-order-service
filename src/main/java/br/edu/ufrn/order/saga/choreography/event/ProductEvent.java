package br.edu.ufrn.order.saga.choreography.event;

public record ProductEvent(
    EventType type,
    String orderId,
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
) implements Event{}
