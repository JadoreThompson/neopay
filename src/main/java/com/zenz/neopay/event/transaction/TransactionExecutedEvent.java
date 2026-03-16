package com.zenz.neopay.event.transaction;

import com.zenz.neopay.enums.TransactionEventType;

import java.math.BigInteger;
import java.util.UUID;

public record TransactionExecutedEvent(
        TransactionEventType type,
        UUID invoiceId,
        String sender,
        String recipient,
        String token,
        BigInteger amount,
        BigInteger timestamp
) implements TransactionEvent {

    public TransactionExecutedEvent(
            UUID invoiceId,
            String sender,
            String recipient,
            String token,
            BigInteger amount,
            BigInteger timestamp
    ) {
        this(
                TransactionEventType.EXECUTED,
                invoiceId,
                sender,
                recipient,
                token,
                amount,
                timestamp
        );
    }
}