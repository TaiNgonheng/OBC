package com.rhbgroup.dte.obc.common;

import com.rhbgroup.dte.obc.common.enums.ResponseMessage;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ResponseStatus {
    Integer code;
    String errorCode;
    String errorMessage;

    public static ResponseStatus success() {
        return ResponseStatus.builder()
                .code(0)
                .build();
    }

    public static ResponseStatus failedWith(ResponseMessage message) {
        return ResponseStatus.builder()
                .code(message.getCode())
                .errorCode(message.getErrorCode())
                .errorMessage(message.getErrorCode())
                .build();
    }
}
