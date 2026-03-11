package com.zenz.crypto_payment_gateway.entity;

import com.zenz.crypto_payment_gateway.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceId;

    private long amountDue;

    private long amountPaid;

    private String currency;

    private int attempts;

    @Embedded
    private List<InvoiceLine> lines;

    private InvoiceStatus status;

    private long createdAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Transaction> transactions;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
