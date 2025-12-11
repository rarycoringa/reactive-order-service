package br.edu.ufrn.order.saga.choreography.event;

public sealed interface Event permits OrderEvent, ProductEvent, PaymentEvent, ShippingEvent {
    EventType type();
    String orderId();
    String productId();
    Integer productQuantity();
    String productName();
    Double productPrice();
    String chargeId();
    String refundId();
    Double amount();
    Integer splitInto();
    String cardNumber();
    String shippingId();
    String address();
}
