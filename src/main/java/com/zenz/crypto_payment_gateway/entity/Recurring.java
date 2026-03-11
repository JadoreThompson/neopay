package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.PricingInterval;
import jakarta.persistence.Embeddable;

@Embeddable
public class Recurring {
    private PricingInterval interval;

    private int intervalCount;
}
