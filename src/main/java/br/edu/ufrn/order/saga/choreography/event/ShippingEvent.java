package br.edu.ufrn.order.saga.choreography.event;

public record ShippingEvent(
    EventType type
) implements Event{}
