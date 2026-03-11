package com.zenz.crypto_payment_gateway.api.route.subscription.request;

import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    private String customerId;
    private String productId;
    private String priceId;
    private int quantity;
}