package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.PricingType;
import com.zenz.crypto_payment_gateway.model.Recurring;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
@Table(name = "prices")
@Entity
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID priceId;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false)
    @Positive
    private long amount;

    @Column(nullable = false)
    private PricingType pricingType;

    @Embedded
    private Recurring recurring;

    @Column(nullable = false)
    private String currency;

    private String metadata;
}
