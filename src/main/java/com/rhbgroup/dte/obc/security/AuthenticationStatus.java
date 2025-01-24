package com.rhbgroup.dte.obc.security;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class AuthenticationStatus {
  public enum Status {
    SUCCESS,
    INVALID,
    EXPIRED,
    BYPASSED
  }
  private Status result;
  private String jwt;

  public static AuthenticationStatus success(String jwt) {
    return AuthenticationStatus.builder()
            .result(Status.SUCCESS)
            .jwt(jwt)
            .build();
  }

  public static AuthenticationStatus invalid() {
    return AuthenticationStatus
            .builder()
            .result(Status.INVALID)
            .build();
  }

  public static AuthenticationStatus expired() {
    return AuthenticationStatus.builder().result(Status.EXPIRED).build();
  }

  public static AuthenticationStatus bypassed() {
    return AuthenticationStatus.builder().result(Status.BYPASSED).build();
  }
}
