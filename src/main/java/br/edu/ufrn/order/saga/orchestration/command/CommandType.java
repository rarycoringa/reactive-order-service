package br.edu.ufrn.order.saga.orchestration.command;

public enum CommandType {
    CREATE_ORDER,
    CANCEL_ORDER,
    FINISH_ORDER,
    RESERVE_PRODUCT,
    RETURN_PRODUCT,
    CHARGE_PAYMENT,
    REFUND_PAYMENT,
    ACCEPT_SHIPPING
}
