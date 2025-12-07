package br.edu.ufrn.order.record;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderResponseDTO(
    String id,
    @JsonProperty("product_id") String productId,
    @JsonProperty("product_quantity") Integer productQuantity,
    @JsonProperty("payment_charge_id") String paymentChargeId,
    @JsonProperty("payment_refund_id") String paymentRefundId,
    @JsonProperty("shipping_id") String shippingId,
    @JsonProperty("created_at") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt
) {}
