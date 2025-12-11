package br.edu.ufrn.order.saga.choreography.event;

public record OrderEvent(
    EventType type
) implements Event{}
