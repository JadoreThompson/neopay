package com.zenz.neopay.event.balance;

import com.zenz.neopay.enums.BalanceEventType;
import com.zenz.neopay.enums.Token;

import java.math.BigInteger;
import java.util.UUID;

public record DecreaseBalance(
        BalanceEventType type,
        UUID transactionId,
        UUID merchantId,
        BigInteger amount,
        Token token
) implements BalanceEvent {

    public DecreaseBalance(UUID transactionId, UUID merchantId, BigInteger amount, Token token) {
        this(BalanceEventType.DECREASE, transactionId, merchantId, amount, token);
    }
}
