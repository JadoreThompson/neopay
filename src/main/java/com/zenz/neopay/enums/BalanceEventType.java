package com.zenz.neopay.enums;

public enum BalanceEventType {
    INCREASE("balance.increase"),
    DECREASE("balance.decrease");

    private final String value;

    BalanceEventType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}