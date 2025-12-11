package br.edu.ufrn.order.saga.orchestration;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.edu.ufrn.order.saga.Saga;
import br.edu.ufrn.order.saga.orchestration.command.Command;
import br.edu.ufrn.order.saga.orchestration.command.CommandType;
import br.edu.ufrn.order.saga.orchestration.command.OrderCommand;
import br.edu.ufrn.order.saga.orchestration.command.PaymentCommand;
import br.edu.ufrn.order.saga.orchestration.command.ProductCommand;
import br.edu.ufrn.order.saga.orchestration.command.ShippingCommand;
import br.edu.ufrn.order.saga.orchestration.event.Event;
import br.edu.ufrn.order.saga.orchestration.event.OrderEvent;
import br.edu.ufrn.order.saga.orchestration.event.PaymentEvent;
import br.edu.ufrn.order.saga.orchestration.event.ProductEvent;
import br.edu.ufrn.order.saga.orchestration.event.ShippingEvent;
import br.edu.ufrn.order.service.OrderService;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Configuration
@Profile("orchestration")
public class Orchestrator implements Saga {
    
    private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);

    private final Sinks.Many<Command> orderCommandSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<Command> orderCommandFlux = orderCommandSink.asFlux();

    private final Sinks.Many<Command> productCommandSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<Command> productCommandFlux = productCommandSink.asFlux();

    private final Sinks.Many<Command> paymentCommandSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<Command> paymentCommandFlux = paymentCommandSink.asFlux();

    private final Sinks.Many<Command> shippingCommandSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<Command> shippingCommandFlux = shippingCommandSink.asFlux();

    private final Sinks.Many<Event> eventSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<Event> eventFlux = eventSink.asFlux();

    @Autowired
    private OrderService orderService;

    @Bean
    public Supplier<Flux<Command>> supplyOrderCommand() {
        return () -> orderCommandFlux
            .doOnNext(command -> logger.info("Supplying order command: {}", command));
    }

    @Bean
    public Supplier<Flux<Command>> supplyProductCommand() {
        return () -> productCommandFlux
            .doOnNext(command -> logger.info("Supplying product command: {}", command));
    }

    @Bean
    public Supplier<Flux<Command>> supplyPaymentCommand() {
        return () -> paymentCommandFlux
            .doOnNext(command -> logger.info("Supplying payment command: {}", command));
    }

    @Bean
    public Supplier<Flux<Command>> supplyShippingCommand() {
        return () -> shippingCommandFlux
            .doOnNext(command -> logger.info("Supplying shipping command: {}", command));
    }

    @Bean
    public Consumer<Flux<Event>> sinkEvent() {
        return flux -> flux
            .doOnNext(event -> logger.info("Sinking event: {}", event))
            .doOnNext(eventSink::tryEmitNext)
            .subscribe();
    }

    public Flux<Command> process() {
        return eventFlux
            .flatMap(this::handleEvent)
            .doOnNext(this::sinkCommand);
    }

    @PostConstruct
    public void startProcessing() {
        process()
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> logger.error("Error processing events: ", error))
            .subscribe();
    }

    @Override
    public Mono<Object> createOrder(
        String productId,
        Integer productQuantity,
        Integer splitInto,
        String cardNumber,
        String address
    ) {
        return Mono
            .just(new OrderCommand(
                CommandType.CREATE_ORDER,
                null,
                productId,
                productQuantity,
                splitInto,
                cardNumber,
                address))
            .doOnNext(orderCommandSink::tryEmitNext)
            .doOnNext(command -> logger.info("Supplied create order command: {}", command))
            .flatMap(command -> Mono.empty())
            .doOnSuccess(orderResponse -> logger.info("Created order response DTO: {}", orderResponse));
    }

    private void sinkCommand(Command command) {
        switch (command) {
            case OrderCommand c -> orderCommandSink.tryEmitNext(c);
            case ProductCommand c -> productCommandSink.tryEmitNext(c);
            case PaymentCommand c -> paymentCommandSink.tryEmitNext(c);
            case ShippingCommand c -> shippingCommandSink.tryEmitNext(c);
        }
    }

    private Flux<Command> handleEvent(Event event) {
        return switch (event) {
            case OrderEvent e -> handleOrderEvent(e);
            case ProductEvent e -> handleProductEvent(e);
            case PaymentEvent e -> handlePaymentEvent(e);
            case ShippingEvent e -> handleShippingEvent(e);
        };
    }

    private Flux<Command> handleOrderEvent(OrderEvent event) {
        return switch (event.type()) {
            case ORDER_CREATED -> Flux.just(
                new ProductCommand(
                    CommandType.RESERVE_PRODUCT,
                    event.orderId(),
                    event.productId(),
                    event.productQuantity()
                )
            );
            case ORDER_CANCELLED -> Flux.empty();
            case ORDER_FINISHED -> Flux.empty();
            default -> Flux.empty();
        };
    }

    private Flux<Command> handleProductEvent(ProductEvent event) {
        return switch (event.type()) {
            case PRODUCT_RESERVED -> orderService
                .retrieveOrder(event.orderId())
                .flatMapMany(order -> Flux.just(
                    new PaymentCommand(
                        CommandType.CHARGE_PAYMENT,
                        event.orderId(),
                        event.productQuantity() * event.productPrice(),
                        order.splitInto(),
                        order.cardNumber()
                    )));
            case PRODUCT_UNAVAILABLE -> orderService
                .retrieveOrder(event.orderId())
                .flatMapMany(order -> Flux.just(
                    new OrderCommand(
                        CommandType.CANCEL_ORDER,
                        event.orderId(),
                        order.productId(),
                        order.productQuantity(),
                        order.splitInto(),
                        order.cardNumber(),
                        order.address()
                    )));
            case PRODUCT_RETURNED -> Flux.empty();
            default -> Flux.empty();
        };
    }

    private Flux<Command> handlePaymentEvent(PaymentEvent event) {
        return switch (event.type()) {
            case PAYMENT_CHARGED -> orderService
                .retrieveOrder(event.orderId())
                .flatMapMany(order -> Flux.just(
                    new ShippingCommand(
                        CommandType.ACCEPT_SHIPPING,
                        event.orderId(),
                        order.address()
                    )));
            case PAYMENT_REFUSED -> orderService
                .retrieveOrder(event.orderId())
                .flatMapMany(order -> Flux.just(
                    new ProductCommand(
                        CommandType.RETURN_PRODUCT,
                        event.orderId(),
                        order.productId(),
                        order.productQuantity()),
                    new OrderCommand(
                        CommandType.CANCEL_ORDER,
                        event.orderId(),
                        order.productId(),
                        order.productQuantity(),
                        order.splitInto(),
                        order.cardNumber(),
                        order.address()
                    )));
            case PAYMENT_REFUNDED -> Flux.empty();
            default -> Flux.empty();
        };
    }

    private Flux<Command> handleShippingEvent(ShippingEvent event) {
        return switch (event.type()) {
            case SHIPPING_ACCEPTED -> orderService
                .retrieveOrder(event.orderId())
                .flatMapMany(order -> Flux.just(
                    new OrderCommand(
                        CommandType.FINISH_ORDER,
                        event.orderId(),
                        order.productId(),
                        order.productQuantity(),
                        order.splitInto(),
                        order.cardNumber(),
                        order.address()
                    )));
            case SHIPPING_REFUSED -> orderService
                .retrieveOrder(event.orderId())
                .flatMapMany(order -> Flux.just(
                    new ProductCommand(
                        CommandType.RETURN_PRODUCT,
                        event.orderId(),
                        order.productId(),
                        order.productQuantity()),
                    new PaymentCommand(
                        CommandType.REFUND_PAYMENT,
                        event.orderId(),
                        null,
                        null,
                        null),
                    new OrderCommand(
                        CommandType.CANCEL_ORDER,
                        event.orderId(),
                        order.productId(),
                        order.productQuantity(),
                        order.splitInto(),
                        order.cardNumber(),
                        order.address()
                    )));
            default -> Flux.empty();
        };
    }

}
