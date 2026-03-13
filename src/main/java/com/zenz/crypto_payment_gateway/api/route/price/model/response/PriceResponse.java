package com.zenz.crypto_payment_gateway.api.route.price.model.response;

import com.zenz.crypto_payment_gateway.enums.PricingType;
import com.zenz.crypto_payment_gateway.model.Recurring;
import lombok.Data;

import java.util.UUID;

@Data
public class PriceResponse {
    private UUID priceId;
    private long amount;
    private PricingType pricingType;
    private String currency;
    private UUID productId;
    private Recurring recurring;
    private String metadata;
}
