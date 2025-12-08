package br.edu.ufrn.order.saga.orchestration.event;

public record ShippingEvent(
    EventType type,
    String orderId,
    String shippingId,
    String address
) implements Event {}
