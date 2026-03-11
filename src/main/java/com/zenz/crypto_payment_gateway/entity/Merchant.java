package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String merchantId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "merchant", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Product> products;

    @OneToMany(mappedBy = "merchant", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Customer> customers;

    @OneToMany(mappedBy = "merchant", fetch = FetchType.LAZY)
    private List<Wallet> wallets;

    @OneToMany(mappedBy = "merchant", fetch = FetchType.LAZY)
    private List<Withdrawal> withdrawals;

    // Operations

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
    }
}