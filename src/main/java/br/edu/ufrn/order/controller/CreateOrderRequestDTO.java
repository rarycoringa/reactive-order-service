package br.edu.ufrn.order.controller;

public record CreateOrderRequestDTO(
    String productId,
    Integer productQuantity,
    Integer splitInto,
    String cardNumber,
    String address
) {}
