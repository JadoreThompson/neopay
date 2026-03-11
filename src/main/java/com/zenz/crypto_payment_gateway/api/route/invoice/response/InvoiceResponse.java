package com.zenz.crypto_payment_gateway.api.route.invoice.response;

import com.zenz.crypto_payment_gateway.enums.InvoiceStatus;
import lombok.Data;

@Data
public class InvoiceResponse {
    private String invoiceId;
    private long amountDue;
    private long amountPaid;
    private String currency;
    private int attempts;
    private InvoiceStatus status;
    private String customerId;
    private long createdAt;
}