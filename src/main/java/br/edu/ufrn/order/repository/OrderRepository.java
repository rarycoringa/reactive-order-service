package br.edu.ufrn.order.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import br.edu.ufrn.order.model.Order;

@Repository
public interface OrderRepository extends ReactiveMongoRepository <Order, String> {
    
}
