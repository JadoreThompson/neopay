package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subscriptionId;

    @Positive
    private int quantity;

    private SubscriptionStatus status;

    @Column(updatable = false)
    private long startedAt;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id", nullable = false)
    private Price price;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}