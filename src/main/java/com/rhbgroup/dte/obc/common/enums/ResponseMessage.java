package com.rhbgroup.dte.obc.common.enums;

public enum ResponseMessage {

    /**
     * Common response messages
     */
    REQUIRED_PARAMS_MISSING("required.params.missing");

    private final String msg;

    ResponseMessage(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
