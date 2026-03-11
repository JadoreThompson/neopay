package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.SubscriptionStatus;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class Subscription {
    private UUID id;

    private SubscriptionStatus status;

    private long startedAt;

    private long createdAt;

    // Relationships
    private Product product;

    private Price price;

    @Positive
    private int quantity;

    // Operations
}
