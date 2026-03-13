package com.zenz.crypto_payment_gateway.model;

import com.zenz.crypto_payment_gateway.enums.PricingInterval;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Embeddable
public class Recurring {
    @NotNull
    private PricingInterval interval;

    @NotNull
    private int intervalCount;
}
