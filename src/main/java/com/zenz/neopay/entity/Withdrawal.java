package com.zenz.neopay.entity;

import com.zenz.neopay.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;


@Data
@Entity
@Table(name = "withdrawals")
public class Withdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID withdrawalId;

    @Column(name="merchant_id",nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name="wallet_id" , nullable = false, updatable = false)
    private UUID walletId;

    @Column(nullable = false, updatable = false)
    private BigInteger amount;

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

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
