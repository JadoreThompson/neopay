package com.zenz.crypto_payment_gateway.api.route.subscription.response;

import com.zenz.crypto_payment_gateway.enums.SubscriptionStatus;
import lombok.Data;

@Data
public class SubscriptionResponse {
    private String subscriptionId;
    private int quantity;
    private SubscriptionStatus status;
    private String customerId;
    private String productId;
    private String priceId;
    private long startedAt;
    private long createdAt;
}