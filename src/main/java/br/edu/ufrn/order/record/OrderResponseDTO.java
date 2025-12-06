package br.edu.ufrn.order.controller;

import java.time.Instant;

public record OrderResponseDTO(
    String id,
    String productId,
    Integer productQuantity,
    String paymentChargeId,
    String paymentRefundId,
    String shippingId,
    Instant createdAt
) {}
