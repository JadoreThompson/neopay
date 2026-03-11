package com.zenz.crypto_payment_gateway.api.route.invoice.request;

import lombok.Data;

@Data
public class CreateInvoiceRequest {
    private long amountDue;
    private String currency;
    private String customerId;
}