package com.zenz.crypto_payment_gateway.api.route.subscription.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubscriptionRequest {
    @NotNull
    private UUID customerId;

    @NotNull
    private UUID productId;

    @NotNull
    private UUID priceId;

    @Min(1)
    private int quantity;
}