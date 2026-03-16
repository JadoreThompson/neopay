package com.zenz.neopay.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID walletId;

    @Column(name="merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private BigInteger balance;

    @Column(nullable = false)
    private BigInteger escrow;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String walletAddress;
}
