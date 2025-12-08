package br.edu.ufrn.order.saga.processor.event;

public record Event(
    EventType type,
    String orderId,
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
 ) {}
