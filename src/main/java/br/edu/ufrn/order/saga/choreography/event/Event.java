package br.edu.ufrn.order.saga.choreography.event;

public sealed interface Event permits OrderEvent, ProductEvent, PaymentEvent, ShippingEvent {
    EventType type();
    String orderId();
    String productId();
    Integer productQuantity();
    Integer splitInto();
    String cardNumber();
    String address();
}
