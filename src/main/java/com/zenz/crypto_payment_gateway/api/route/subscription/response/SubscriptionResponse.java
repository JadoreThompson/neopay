package com.zenz.crypto_payment_gateway.api.route.subscription.response;

import com.zenz.crypto_payment_gateway.enums.SubscriptionStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionResponse {
    private UUID subscriptionId;
    private int quantity;
    private SubscriptionStatus status;
    private UUID customerId;
    private UUID productId;
    private UUID priceId;
    private long startedAt;
    private long createdAt;
}