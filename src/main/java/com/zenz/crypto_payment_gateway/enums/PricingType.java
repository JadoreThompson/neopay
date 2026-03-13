package com.zenz.crypto_payment_gateway.enums;

public enum PricingType {
    ONE_TIME,
    RECURRING;

    @Override
    public String toString() {
        return name();
    }
}
