package com.zenz.neopay.enums;

public enum TransactionEventType {
    EXECUTED("transaction.executed"),
    FAILED("transaction.failed");

    private final String value;

    TransactionEventType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
