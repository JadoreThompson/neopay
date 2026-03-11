package com.zenz.crypto_payment_gateway.api.route.transaction.response;

import com.zenz.crypto_payment_gateway.enums.TransactionStatus;
import lombok.Data;

@Data
public class TransactionResponse {
    private String transactionId;
    private long amountExpected;
    private long amountReceived;
    private String currency;
    private String chain;
    private String senderWalletAddress;
    private String recipientWalletAddress;
    private TransactionStatus status;
    private String invoiceId;
    private long createdAt;
    private long completedAt;
}