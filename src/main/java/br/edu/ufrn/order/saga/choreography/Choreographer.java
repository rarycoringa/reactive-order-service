package br.edu.ufrn.order.saga.choreography;

import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.edu.ufrn.order.saga.Saga;
import br.edu.ufrn.order.saga.choreography.event.EventType;
import br.edu.ufrn.order.saga.choreography.event.OrderEvent;
import br.edu.ufrn.order.saga.choreography.event.PaymentEvent;
import br.edu.ufrn.order.saga.choreography.event.ProductEvent;
import br.edu.ufrn.order.saga.choreography.event.ShippingEvent;
import br.edu.ufrn.order.service.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Configuration
@Profile("choreography")
public class Choreographer implements Saga {
    
    private static final Logger logger = LoggerFactory.getLogger(Choreographer.class);

    @Autowired
    private OrderService orderService;

    private final Sinks.Many<OrderEvent> eventSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<OrderEvent> eventFlux = eventSink.asFlux();

    @Bean
    public Supplier<Flux<OrderEvent>> publishOrderEvent() {
        return () -> eventFlux
            .concatMap(this::process)
            .doOnNext(event -> logger.info("Publishing order event: {}", event));
    }

    @Bean
    public Function<Flux<ProductEvent>, Flux<OrderEvent>> processProductEvent() {
        return flux -> flux
            .doOnNext(event -> logger.info("Received product event: {}", event))
            .concatMap(this::process)
            .doOnNext(event -> logger.info("Sending order event: {}", event));
    }

    @Bean
    public Function<Flux<PaymentEvent>, Flux<OrderEvent>> processPaymentEvent() {
        return flux -> flux
            .doOnNext(event -> logger.info("Received payment event: {}", event))
            .concatMap(this::process)
            .doOnNext(event -> logger.info("Sending order event: {}", event));
    }

    @Bean
    public Function<Flux<ShippingEvent>, Flux<OrderEvent>> processShippingEvent() {
        return flux -> flux
            .doOnNext(event -> logger.info("Received shipping event: {}", event))
            .concatMap(this::process)
            .doOnNext(event -> logger.info("Sending order event: {}", event));
    }

    @Override
    public Mono<Object> createOrder(
        String productId,
        Integer productQuantity,
        Integer splitInto,
        String cardNumber,
        String address
    ) {
        eventSink.tryEmitNext(new OrderEvent(
            EventType.ORDER_CREATED,
            null,
            productId,
            productQuantity,
            null,
            null,
            null,
            null,
            null,
            splitInto,
            cardNumber,
            null,
            address));
        return Mono.empty();
    }

    private Mono<OrderEvent> process(OrderEvent event) {
        return switch (event.type()) {
            case ORDER_CREATED -> orderService.createOrder(event.productId(), event.productQuantity(), event.splitInto(), event.cardNumber(), event.address())
                .map(order -> new OrderEvent(
                    EventType.ORDER_CREATED,
                    order.id(),
                    order.productId(),
                    order.productQuantity(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    order.splitInto(),
                    order.cardNumber(),
                    null,
                    order.address()));
            
            default -> Mono.empty();
        };
    }

    private Mono<OrderEvent> process(ProductEvent event) {
        return switch (event.type()) {
            case PRODUCT_UNAVAILABLE -> Mono.just(new OrderEvent(
                EventType.ORDER_CANCELLED,
                event.orderId(),
                event.productId(),
                event.productQuantity(),
                event.productName(),
                event.productPrice(),
                event.chargeId(),
                event.refundId(),
                event.amount(),
                event.splitInto(),
                event.cardNumber(),
                event.shippingId(),
                event.address()));

            case PRODUCT_RESERVED, PRODUCT_RETURNED -> Mono.empty();

            default -> Mono.empty();
        };
    }

        private Mono<OrderEvent> process(PaymentEvent event) {
        return switch (event.type()) {
            case PAYMENT_REFUSED -> Mono.just(new OrderEvent(
                EventType.ORDER_CANCELLED,
                event.orderId(),
                event.productId(),
                event.productQuantity(),
                event.productName(),
                event.productPrice(),
                event.chargeId(),
                event.refundId(),
                event.amount(),
                event.splitInto(),
                event.cardNumber(),
                event.shippingId(),
                event.address()));

            case PAYMENT_CHARGED, PAYMENT_REFUNDED -> Mono.empty();

            default -> Mono.empty();
        };
    }

        private Mono<OrderEvent> process(ShippingEvent event) {
        return switch (event.type()) {
            case SHIPPING_REFUSED -> Mono.just(new OrderEvent(
                EventType.ORDER_CANCELLED,
                event.orderId(),
                event.productId(),
                event.productQuantity(),
                event.productName(),
                event.productPrice(),
                event.chargeId(),
                event.refundId(),
                event.amount(),
                event.splitInto(),
                event.cardNumber(),
                event.shippingId(),
                event.address()));

            case SHIPPING_ACCEPTED -> Mono.just(new OrderEvent(
                EventType.ORDER_FINISHED,
                event.orderId(),
                event.productId(),
                event.productQuantity(),
                event.productName(),
                event.productPrice(),
                event.chargeId(),
                event.refundId(),
                event.amount(),
                event.splitInto(),
                event.cardNumber(),
                event.shippingId(),
                event.address()));

            default -> Mono.empty();
        };
    }

}
