package com.rhbgroup.dte.obc.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum UserAuthenticationMessage {

    INVALID_CREDENTIAL("00001", "user.bad_credential"),
    TOKEN_HAS_BEEN_EXPIRED("00002", "user.token_expired"),
    USER_NOT_EXISTS("00003", "user.not_exist");

    String code;
    String msg;
}
