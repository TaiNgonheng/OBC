package com.rhbgroup.dte.obc.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResponseMessage {

    INVALID_CREDENTIAL(1, "00001", "user.bad_credential"),
    TOKEN_HAS_BEEN_EXPIRED(2, "00002", "user.token_expired"),
    USER_NOT_EXISTS(3, "00003", "user.not_exist"),
    REQUIRED_PARAMS_MISSING(0, "00004", "required.params.missing"),
    ;

    private final Integer code;
    private final String errorCode;
    private final String msg;
}
