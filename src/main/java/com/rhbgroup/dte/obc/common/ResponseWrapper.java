package com.rhbgroup.dte.obc.common;

import com.rhbgroup.dte.obc.common.enums.ResponseMessage;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class ResponseWrapper<T> {

    ResponseStatus status;
    T data;

    public static <T> ResponseWrapper<T> ok(T data) {
        return ResponseWrapper.<T>builder()
                .status(ResponseStatus.success())
                .data(data)
                .build();
    }

    public static <T> ResponseWrapper<T> failed(ResponseMessage message) {
        return ResponseWrapper.<T>builder()
                .status(ResponseStatus.failedWith(message))
                .data(null)
                .build();
    }
}
