package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.WithdrawalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;


@Data
@Entity
@Table(name = "withdrawals")
public class Withdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID withdrawalId;

    @Positive
    @Column(nullable = false, updatable = false)
    private long amount;

    @Column(nullable = false, updatable = false)
    private String currency;

    @Column(nullable = false, updatable = false)
    private String chain;

    @Column(nullable = false, updatable = false)
    private String txnAddress;

    @Column(nullable = false, updatable = false)
    private WithdrawalStatus status;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    private long completedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
