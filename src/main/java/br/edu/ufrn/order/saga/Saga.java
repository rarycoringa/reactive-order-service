package br.edu.ufrn.order.saga;

import reactor.core.publisher.Mono;

public interface Saga {
    
    public Mono<Object> createOrder(
        String productId,
        Integer productQuantity,
        Integer splitInto,
        String cardNumber,
        String address
    );

}
