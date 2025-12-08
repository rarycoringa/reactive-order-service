package br.edu.ufrn.order.saga.orchestration.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderCommand.class, name = "CREATE_ORDER"),
    @JsonSubTypes.Type(value = OrderCommand.class, name = "CANCEL_ORDER"),
    @JsonSubTypes.Type(value = OrderCommand.class, name = "FINISH_ORDER"),
    @JsonSubTypes.Type(value = ProductCommand.class, name = "RESERVE_PRODUCT"),
    @JsonSubTypes.Type(value = ProductCommand.class, name = "RETURN_PRODUCT"),
    @JsonSubTypes.Type(value = PaymentCommand.class, name = "CHARGE_PAYMENT"),
    @JsonSubTypes.Type(value = PaymentCommand.class, name = "REFUND_PAYMENT"),
    @JsonSubTypes.Type(value = ShippingCommand.class, name = "ACCEPT_SHIPPING")
})
public sealed interface Command permits OrderCommand, ProductCommand, PaymentCommand, ShippingCommand {
    CommandType type();
    String orderId();
}
