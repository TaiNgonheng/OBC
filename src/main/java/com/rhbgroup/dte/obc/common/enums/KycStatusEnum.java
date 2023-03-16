package com.rhbgroup.dte.obc.common.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum KycStatusEnum {

  FULL_KYC("FULL_KYC"),
  PARTIAL_KYC("PARTIAL_KYC"),
  REJECTED("REJECTED"),
  UNKNOWN("UNKNOWN"),
  ;

  private final String name;

  public static KycStatusEnum parse(String name) {
    return Arrays.stream(KycStatusEnum.values())
        .filter(kyc -> kyc.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElse(UNKNOWN);
  }
}
