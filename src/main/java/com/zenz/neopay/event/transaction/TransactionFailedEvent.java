package com.zenz.neopay.event.transaction;

import com.zenz.neopay.enums.TransactionEventType;

import java.math.BigInteger;
import java.util.UUID;

public record TransactionFailedEvent(
        TransactionEventType  type,
        UUID invoiceId,
        String sender,
        String recipient,
        String token,
        BigInteger amount,
        String reason,
        BigInteger timestamp
) implements TransactionEvent {

    public TransactionFailedEvent(
            UUID invoiceId,
            String sender,
            String recipient,
            String token,
            BigInteger amount,
            String reason,
            BigInteger timestamp
    ) {
        this (
                TransactionEventType.FAILED,
                invoiceId,
                sender,
                recipient,
                token,
                amount,
                reason,
                timestamp
        );
    }
}