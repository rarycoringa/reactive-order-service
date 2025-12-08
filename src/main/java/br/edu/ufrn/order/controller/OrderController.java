package br.edu.ufrn.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufrn.order.record.CreateOrderRequestDTO;
import br.edu.ufrn.order.record.OrderResponseDTO;
import br.edu.ufrn.order.service.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/oders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    // @PostMapping
    // public Mono<OrderResponseDTO> createOrder(@RequestBody CreateOrderRequestDTO body) {
    //     return orderService.createOrder(body);
    // }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderResponseDTO> retrieveOrders() {
        return orderService.retrieveOrders();
    }

    @GetMapping("/{id}")
    public Mono<OrderResponseDTO> retrieveOrder(@PathVariable String id) {
        return orderService.retrieveOrder(id);
    }
}
