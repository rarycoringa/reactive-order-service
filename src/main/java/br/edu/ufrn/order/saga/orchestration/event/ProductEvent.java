package br.edu.ufrn.order.saga.orchestration.event;

public record ProductEvent(
    EventType type,
    String orderId,
    String productId,
    String productName,
    Integer productQuantity,
    Double productPrice
) implements Event {}
