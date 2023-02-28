package com.rhbgroup.dte.obc.common.enums;

public enum StatusEnum {
    INACTIVE(0),
    ACTIVE(1),
    DRAFTED(2), // or cloned
    UNDEFINED(-1);

    private final int value;

    StatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StatusEnum parse(int value) {
        for (StatusEnum statusEnum : StatusEnum.values()) {
            if (statusEnum.getValue() == value) {
                return statusEnum;
            }
        }
        return UNDEFINED;
    }
}
