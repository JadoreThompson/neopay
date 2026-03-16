package com.zenz.neopay.event.balance;

import com.zenz.neopay.enums.BalanceEventType;
import com.zenz.neopay.enums.Token;

import java.math.BigInteger;
import java.util.UUID;

public record IncreaseBalance(
        BalanceEventType type,
        UUID transactionId,
        UUID merchantId,
        BigInteger amount,
        Token token
) implements BalanceEvent {

    public IncreaseBalance(UUID transactionId, UUID merchantId, BigInteger amount, Token token) {
        this(BalanceEventType.INCREASE, transactionId, merchantId, amount, token);
    }
}
