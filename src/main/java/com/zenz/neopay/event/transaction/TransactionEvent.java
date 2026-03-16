package com.zenz.neopay.event.transaction;

import java.math.BigInteger;
import java.util.UUID;

public interface TransactionEvent {
    UUID transactionId();

    String sender();

    String recipient();

    String token();

    BigInteger amount();

    BigInteger timestamp();
}