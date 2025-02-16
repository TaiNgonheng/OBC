package com.rhbgroup.dte.obc.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class CustomBizException extends RuntimeException {

  private final Integer code;
  private final String msg;
}
