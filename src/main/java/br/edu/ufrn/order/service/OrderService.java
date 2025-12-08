package br.edu.ufrn.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ufrn.order.model.Order;
import br.edu.ufrn.order.record.OrderResponseDTO;
import br.edu.ufrn.order.repository.OrderRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public Mono<OrderResponseDTO> createOrder(String productId, Integer productQuantity, Integer splitInto, String cardNumber, String address) {
        return orderRepository
            .save(new Order(
                    productId,
                    productQuantity,
                    splitInto,
                    cardNumber,
                    address
                ))
            .map(order -> new OrderResponseDTO(
                order.getId(),
                order.getProductId(), 
                order.getProductQuantity(),
                order.getSplitInto(),
                order.getCardNumber(),
                order.getAddress(),
                order.getCreatedAt()))
            .doOnSuccess(order -> logger.info("Order successfully created: id={}", order.id()));
    }

    public Flux<OrderResponseDTO> retrieveOrders() {
        return orderRepository
            .findAll()
            .map(order -> new OrderResponseDTO(
                order.getId(),
                order.getProductId(), 
                order.getProductQuantity(),
                order.getSplitInto(),
                order.getCardNumber(),
                order.getAddress(),
                order.getCreatedAt()))
            .doOnNext(order -> logger.info("Order successfully retrieved: id={}", order.id()));
    }

    public Mono<OrderResponseDTO> retrieveOrder(String orderId) {
        return orderRepository
            .findById(orderId)
            .switchIfEmpty(Mono.empty())
            .map(order -> new OrderResponseDTO(
                order.getId(),
                order.getProductId(), 
                order.getProductQuantity(),
                order.getSplitInto(),
                order.getCardNumber(),
                order.getAddress(),
                order.getCreatedAt()))
            .doOnSuccess(order -> logger.info("Order successfully retrieved: id={}", order.id()));
    }

}
