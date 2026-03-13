package com.zenz.crypto_payment_gateway.api.route.subscription.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubscriptionRequest {
    private UUID customerId;
    private UUID productId;
    private UUID priceId;
    private int quantity;
}