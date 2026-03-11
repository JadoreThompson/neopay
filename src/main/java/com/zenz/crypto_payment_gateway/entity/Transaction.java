package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @Column(nullable = false, updatable = false)
    @Positive
    private long amountExpected;

    @Positive
    private long amountReceived;

    @Column(nullable = false, updatable = false)
    private String currency;

    @Column(nullable = false, updatable = false)
    private String chain;

    @Column(nullable = false, updatable = false)
    private String txnAddress;

    @Column(nullable = false, updatable = false)
    private String senderWalletAddress;

    @Column(nullable = false, updatable = false)
    private String recipientWalletAddress;

    @Column(nullable = false)
    private TransactionStatus status;

    private String metadata;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    private long completedAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}