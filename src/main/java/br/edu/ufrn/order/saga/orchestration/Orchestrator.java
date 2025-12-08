package br.edu.ufrn.order.saga.orchestration;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.edu.ufrn.order.record.OrderResponseDTO;
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

@Configuration
@Profile("orchestration")
public class Orchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);

    private final Sinks.Many<Event> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Flux<Event> flux = sink.asFlux();

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private OrderService orderService;

    private static final String ORDER_COMMAND_QUEUE = "order_commands";
    private static final String PRODUCT_COMMAND_QUEUE = "product_commands";
    private static final String PAYMENT_COMMAND_QUEUE = "payment_commands";
    private static final String SHIPPING_COMMAND_QUEUE = "shipping_commands";

    @Bean
    public Consumer<Event> sinkEvent() {
        return event -> sink.tryEmitNext(event);
    }

    @PostConstruct
    public void subscribeSupplyCommands() {
        supplyCommands().subscribe();
    }

    public Flux<Command> supplyCommands() {
        return flux
            .flatMap(this::handleEvent)
            .doOnNext(this::sendCommand)
            .share();
    }

    public Mono<OrderResponseDTO> emitCreateOrderCommand(
        String productId, Integer productQuantity, Integer splitInto, String cardNumber, String address
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
            .doOnNext(this::sendCommand)
            .map(command -> new OrderResponseDTO(
                null,
                command.productId(),
                command.productQuantity(),
                command.splitInto(),
                command.cardNumber(),
                command.address(),
                null));
    }

    private Flux<Command> handleEvent(Event event) {
        logger.info("Handling event: {}", event);
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

    private String chooseDestination(Command command) {
        return switch (command) {
            case OrderCommand e -> ORDER_COMMAND_QUEUE;
            case ProductCommand e -> PRODUCT_COMMAND_QUEUE;
            case PaymentCommand e -> PAYMENT_COMMAND_QUEUE;
            case ShippingCommand e -> SHIPPING_COMMAND_QUEUE;
        };
    }

    private void sendCommand(Command command) {
        String destination = chooseDestination(command);
        streamBridge.send(destination, command);
    }

}
