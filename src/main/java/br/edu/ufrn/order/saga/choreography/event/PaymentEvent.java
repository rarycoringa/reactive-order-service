package br.edu.ufrn.order.saga.choreography.event;

public record PaymentEvent(
    EventType type
) implements Event{}
