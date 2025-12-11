package br.edu.ufrn.order.saga.choreography.event;

public record ProductEvent(
    EventType type
) implements Event{}
