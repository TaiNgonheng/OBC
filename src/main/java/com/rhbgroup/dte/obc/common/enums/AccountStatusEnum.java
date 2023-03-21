package com.rhbgroup.dte.obc.common.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatusEnum {
  ACTIVATED("ACTIVATED"),
  DEACTIVATED("DEACTIVATED"),
  UNKNOWN("UNKNOWN"),
  ;

  private final String status;

  public static AccountStatusEnum parse(String name) {
    return Arrays.stream(AccountStatusEnum.values())
        .filter(accountStatus -> accountStatus.getStatus().equalsIgnoreCase(name))
        .findFirst()
        .orElse(UNKNOWN);
  }
}
