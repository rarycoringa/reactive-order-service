package br.edu.ufrn.order.saga.choreography;

import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.edu.ufrn.order.saga.Saga;
import br.edu.ufrn.order.saga.choreography.event.EventType;
import br.edu.ufrn.order.saga.choreography.event.OrderEvent;
import br.edu.ufrn.order.saga.choreography.event.PaymentEvent;
import br.edu.ufrn.order.saga.choreography.event.ProductEvent;
import br.edu.ufrn.order.saga.choreography.event.ShippingEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Configuration
@Profile("choreography")
public class Choreographer implements Saga {
    
    private static final Logger logger = LoggerFactory.getLogger(Choreographer.class);

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
            .concatMap(this::handleProductEvent)
            .concatMap(this::process)
            .doOnNext(event -> logger.info("Sending order event: {}", event));
    }

    @Bean
    public Function<Flux<PaymentEvent>, Flux<OrderEvent>> processPaymentEvent() {
        return flux -> flux
            .doOnNext(event -> logger.info("Received payment event: {}", event))
            .concatMap(this::handlePaymentEvent)
            .concatMap(this::process)
            .doOnNext(event -> logger.info("Sending order event: {}", event));
    }

    @Bean
    public Function<Flux<ShippingEvent>, Flux<OrderEvent>> processShippingEvent() {
        return flux -> flux
            .doOnNext(event -> logger.info("Received shipping event: {}", event))
            .concatMap(this::handleShippingEvent)
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
        eventSink.tryEmitNext(new OrderEvent(EventType.ORDER_CREATED));
        return Mono.empty();
    }

    private Mono<OrderEvent> handleProductEvent(ProductEvent event) {
        return switch (event.type()) {
            case PRODUCT_UNAVAILABLE -> Mono.just(new OrderEvent(EventType.ORDER_CANCELLED));
            case PRODUCT_RESERVED, PRODUCT_RETURNED -> Mono.empty();
            default -> Mono.empty();
        };
    }

    private Mono<OrderEvent> handlePaymentEvent(PaymentEvent event) {
        return switch (event.type()) {
            case PAYMENT_REFUSED -> Mono.just(new OrderEvent(EventType.ORDER_CANCELLED));
            case PAYMENT_CHARGED, PAYMENT_REFUNDED -> Mono.empty();
            default -> Mono.empty();
        };
    }

    private Mono<OrderEvent> handleShippingEvent(ShippingEvent event) {
        return switch (event.type()) {
            case SHIPPING_REFUSED -> Mono.just(new OrderEvent(EventType.ORDER_CANCELLED));
            case SHIPPING_ACCEPTED -> Mono.just(new OrderEvent(EventType.ORDER_FINISHED));
            default -> Mono.empty();
        };
    }

    private Mono<OrderEvent> process(OrderEvent event) {
        return switch (event.type()) {
            case ORDER_CREATED -> Mono.just(event)
                .doOnNext(e -> logger.info("Order created: {}", e));

            case ORDER_CANCELLED -> Mono.just(event)
                .doOnNext(e -> logger.info("Order cancelled: {}", e));

            case ORDER_FINISHED -> Mono.just(event)
                .doOnNext(e -> logger.info("Order finished: {}", e));

            default -> Mono.empty();
        };
    }

}
