package com.rhbgroup.dte.obc.exceptions;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserAuthenticationException extends RuntimeException {

    private final ResponseMessage responseMessage;
}
