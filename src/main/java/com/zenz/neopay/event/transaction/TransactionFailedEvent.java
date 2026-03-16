package com.zenz.neopay.event.transaction;

import java.math.BigInteger;
import java.util.UUID;

public record TransactionFailedEvent(
        String transactionKey,
        UUID transactionId,
        String sender,
        String recipient,
        String token,
        BigInteger amount,
        String reason,
        BigInteger timestamp
) implements TransactionEvent {
}