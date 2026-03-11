package com.zenz.crypto_payment_gateway.api.route.price.response;

import com.zenz.crypto_payment_gateway.enums.PricingType;
import lombok.Data;

@Data
public class PriceResponse {
    private String priceId;
    private long amount;
    private PricingType pricingType;
    private String currency;
    private String productId;
    private Integer recurringInterval;
    private String recurringIntervalType;
}