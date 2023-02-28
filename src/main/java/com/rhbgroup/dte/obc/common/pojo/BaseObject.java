package com.rhbgroup.dte.obc.common.pojo;

import lombok.Data;

import java.time.Instant;

@Data
public class BaseObject {
    private String createdBy;
    private Instant createAt;
    private Instant updateAt;
    private Instant deleteAt;
}
