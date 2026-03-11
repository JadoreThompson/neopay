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
    private UUID id;

    private long amountDue;

    private long amountPaid;

    private String currency;

    private int attempts;

    @Embedded
    private List<InvoiceLine> lines;

    private InvoiceStatus status;

    private long createdAt;

    // Relationships

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
