package com.rhbgroup.dte.obc.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserAuthenticationException extends RuntimeException {

    private final UserAuthenticationMessage authenticationMessage;
}
