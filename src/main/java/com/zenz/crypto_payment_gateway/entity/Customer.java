package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="customers")
public class Customer {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String customerId;

    @Column(nullable = false)
    private String nickname;

    private String email;

    private byte[] metadata;

    @Column(nullable = false, updatable = false)
    private long createdAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Invoice> invoices;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Subscription> subscriptions;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
    }
}
