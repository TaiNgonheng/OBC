package com.rhbgroup.dte.obc.security;

import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class AuthenticationResponse {
    AuthenticationStatusEnum result;
    String jwt;
}
