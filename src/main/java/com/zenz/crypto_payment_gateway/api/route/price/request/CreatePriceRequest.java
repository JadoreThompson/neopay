package com.zenz.crypto_payment_gateway.api.route.price.request;

import com.zenz.crypto_payment_gateway.enums.PricingType;
import lombok.Data;

@Data
public class CreatePriceRequest {
    private long amount;
    private PricingType pricingType;
    private String currency;
    private String productId;
    private Integer recurringInterval;
    private String recurringIntervalType;
}