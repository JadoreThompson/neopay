package com.zenz.crypto_payment_gateway.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class InvoiceLine {
    private long amount;

    private String description;
}
