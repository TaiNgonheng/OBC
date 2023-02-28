package com.rhbgroup.dte.obc.common;

import java.time.Instant;

public interface BaseORM {

    String getCreatedBy();

    Instant getDeleteAt();

    Instant getCreateAt();

    Instant getUpdateAt();
}
