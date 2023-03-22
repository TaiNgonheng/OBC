package com.rhbgroup.dte.obc.common;

import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.model.ResponseStatus;

public class ResponseHandler {

  private ResponseHandler() {}

  public static ResponseStatus ok() {
    return new ResponseStatus()
        .code(AppConstants.STATUS.SUCCESS)
        .errorCode(null)
        .errorMessage(null);
  }

  public static ResponseStatus error(ResponseMessage message) {
    return new ResponseStatus()
        .code(AppConstants.STATUS.ERROR)
        .errorCode(message.getCode().toString())
        .errorMessage(message.getMsg());
  }
}
