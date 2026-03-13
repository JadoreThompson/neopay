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

    @Column(nullable = false, updatable = false)
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

    // Relationships

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
