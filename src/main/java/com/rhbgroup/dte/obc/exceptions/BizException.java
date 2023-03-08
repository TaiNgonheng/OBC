package com.rhbgroup.dte.obc.exceptions;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BizException extends RuntimeException {

  private final ResponseMessage responseMessage;
}
