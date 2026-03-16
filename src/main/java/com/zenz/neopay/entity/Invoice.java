package com.zenz.neopay.entity;

import com.zenz.neopay.enums.InvoiceStatus;
import com.zenz.neopay.model.InvoiceLine;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceId;

    @Column(name="merchant_id" , nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name="customer_id" , nullable = false, updatable = false)
    private UUID customerId;

    private BigInteger amountDue;

    private BigInteger amountPaid;

    private String currency;

    private int attempts;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<InvoiceLine> lines;

    private String metadata;

    private InvoiceStatus status;

    private long createdAt;

    // Operations

    @PrePersist
    public void prePersist() {
        createdAt = System.currentTimeMillis();
    }
}
