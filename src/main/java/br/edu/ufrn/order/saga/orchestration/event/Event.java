package br.edu.ufrn.order.saga.orchestration.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderEvent.class, name = "ORDER_CREATED"),
    @JsonSubTypes.Type(value = OrderEvent.class, name = "ORDER_CANCELLED"),
    @JsonSubTypes.Type(value = OrderEvent.class, name = "ORDER_FINISHED"),
    @JsonSubTypes.Type(value = ProductEvent.class, name = "PRODUCT_RESERVED"),
    @JsonSubTypes.Type(value = ProductEvent.class, name = "PRODUCT_UNAVAILABLE"),
    @JsonSubTypes.Type(value = ProductEvent.class, name = "PRODUCT_RETURNED"),
    @JsonSubTypes.Type(value = PaymentEvent.class, name = "PAYMENT_CHARGED"),
    @JsonSubTypes.Type(value = PaymentEvent.class, name = "PAYMENT_REFUSED"),
    @JsonSubTypes.Type(value = PaymentEvent.class, name = "PAYMENT_REFUNDED"),
    @JsonSubTypes.Type(value = ShippingEvent.class, name = "SHIPPING_ACCEPTED"),
    @JsonSubTypes.Type(value = ShippingEvent.class, name = "SHIPPING_REFUSED")
})
public sealed interface Event permits OrderEvent, ProductEvent, PaymentEvent, ShippingEvent {
    EventType type();
    String orderId();
}
