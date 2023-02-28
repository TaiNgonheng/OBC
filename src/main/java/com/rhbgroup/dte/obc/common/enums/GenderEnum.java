package com.rhbgroup.dte.obc.common.enums;

public enum GenderEnum {
    MALE("MALE"),
    FEMALE("FEMALE"),
    OTHER("OTHER");

    private final String value;

    GenderEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GenderEnum parse(String value) {
        for (GenderEnum genderEnum : GenderEnum.values()) {
            if (genderEnum.getValue().equals(value)) {
                return genderEnum;
            }
        }
        return OTHER;
    }
}
