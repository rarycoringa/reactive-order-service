package br.edu.ufrn.order.saga.processor;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.edu.ufrn.order.saga.processor.command.Command;
import br.edu.ufrn.order.saga.processor.event.Event;
import br.edu.ufrn.order.saga.processor.event.EventType;
import br.edu.ufrn.order.service.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@Profile("orchestration")
public class CommandProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    @Autowired
    private OrderService orderService;

    @Bean
    public Function<Flux<Command>, Flux<Event>> processOrderCommand() {
        return flux -> flux
            .concatMap(this::process);
    }

    private Mono<Event> process(Command command) {
        return switch (command.type()) {
            case CREATE_ORDER -> createOrder(command);
            case CANCEL_ORDER -> cancelOrder(command);
            case FINISH_ORDER -> finishOrder(command);
        };
    }

    private Mono<Event> createOrder(Command command) {
        return orderService
            .createOrder(
                command.productId(),
                command.productQuantity(),
                command.splitInto(),
                command.cardNumber(),
                command.address())
            .map(order -> new Event(
                EventType.ORDER_CREATED,
                order.id(),
                order.productId(),
                order.productQuantity(),
                order.splitInto(),
                order.cardNumber(),
                order.address()))
            .doOnSuccess(orderEvent -> logger.info("Order created: {}", orderEvent));
    }

    private Mono<Event> cancelOrder(Command command) {
        return Mono.just(
            new Event(
                EventType.ORDER_CANCELLED,
                command.orderId(),
                command.productId(),
                command.productQuantity(),
                command.splitInto(),
                command.cardNumber(),
                command.address()))
            .doOnSuccess(orderEvent -> logger.info("Order cancelled: {}", orderEvent));
    }

    private Mono<Event> finishOrder(Command command) {
        return Mono.just(
            new Event(
                EventType.ORDER_FINISHED,
                command.orderId(),
                command.productId(),
                command.productQuantity(),
                command.splitInto(),
                command.cardNumber(),
                command.address()))
            .doOnSuccess(orderEvent -> logger.info("Order finished: {}", orderEvent));
    }

}
