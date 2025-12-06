package br.edu.ufrn.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ufrn.order.controller.CreateOrderRequestDTO;
import br.edu.ufrn.order.controller.OrderResponseDTO;
import br.edu.ufrn.order.model.Order;
import br.edu.ufrn.order.repository.OrderRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public Mono<OrderResponseDTO> createOrder(CreateOrderRequestDTO orderRequest) {
        return orderRepository
            .save(new Order(
                orderRequest.productId(),
                orderRequest.productQuantity()))
            .map(order -> new OrderResponseDTO(
                order.getId(),
                order.getProductId(), 
                order.getProductQuantity(),
                order.getPaymentChargeId(),
                order.getPaymentRefundId(),
                order.getShippingId(),
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
                order.getPaymentChargeId(),
                order.getPaymentRefundId(),
                order.getShippingId(),
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
                order.getPaymentChargeId(),
                order.getPaymentRefundId(),
                order.getShippingId(),
                order.getCreatedAt()))
            .doOnSuccess(order -> logger.info("Order successfully retrieved: id={}", order.id()));
    }

    public Mono<String> updatePaymentChargeId(String orderId, String paymentChargeId) {
        return orderRepository
            .findById(orderId)
            .flatMap(order -> {
                order.setPaymentChargeId(paymentChargeId);
                return orderRepository.save(order);
            })
            .map(Order::getId)
            .doOnSuccess(id -> logger.info("Payment Charge Id successfully updated: id={}, paymentChargeId={}", id, paymentChargeId));
    }

    public Mono<String> updatePaymentRefundId(String orderId, String paymentRefundId) {
        return orderRepository
            .findById(orderId)
            .flatMap(order -> {
                order.setPaymentRefundId(paymentRefundId);
                return orderRepository.save(order);
            })
            .map(Order::getId)
            .doOnSuccess(id -> logger.info("Payment Refund Id successfully updated: id={}, paymentRefundId={}", id, paymentRefundId));
    }

    public Mono<String> updateShippingId(String orderId, String shippingId) {
        return orderRepository
            .findById(orderId)
            .flatMap(order -> {
                order.setShippingId(shippingId);
                return orderRepository.save(order);
            })
            .map(Order::getId)
            .doOnSuccess(id -> logger.info("Shipping Id successfully updated: id={}, shippingId={}", id, shippingId));
    }

}
