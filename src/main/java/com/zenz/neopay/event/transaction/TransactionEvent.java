package com.zenz.neopay.event.transaction;

import com.zenz.neopay.enums.TransactionEventType;

import java.math.BigInteger;
import java.util.UUID;

public interface TransactionEvent {
    TransactionEventType type();

    UUID invoiceId();

    String sender();

    String recipient();

    String token();

    BigInteger amount();

    BigInteger timestamp();
}