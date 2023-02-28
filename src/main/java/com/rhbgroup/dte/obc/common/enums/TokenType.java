package com.rhbgroup.dte.obc.common.enums;

public enum TokenType {
    RESET_PASSWORD("RESET_PASSWORD"),
    UNDEFINED("UNDEFINED");

    private final String value;

    TokenType(String type) {
        this.value = type;
    }

    public String getValue() {
        return value;
    }

    public static TokenType parse(String value) {
        for (TokenType tokenType : TokenType.values()) {
            if (tokenType.getValue().equals(value)) {
                return tokenType;
            }
        }
        return UNDEFINED;
    }
}
