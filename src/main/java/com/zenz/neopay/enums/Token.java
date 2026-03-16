package com.zenz.neopay.enums;

public enum Token {
    ETHEREUM("ETH");

    private final String value;

    Token(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
