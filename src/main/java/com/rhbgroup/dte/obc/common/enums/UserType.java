package com.rhbgroup.dte.obc.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UserType {
    ADMIN(ROLE.ADMIN),
    USER(ROLE.USER),
    UNDEFINED(ROLE.UNDEFINED);

    public static class ROLE {
        public static final String ADMIN = "ADMIN";
        public static final String USER = "USER";
        public static final String UNDEFINED = "UNDEFINED";
    }

    String value;

    public static UserType parse(String value) {
        return Arrays.stream(UserType.values())
                .filter(userType -> userType.getValue().equals(value))
                .findFirst()
                .orElse(UNDEFINED);
    }
}
