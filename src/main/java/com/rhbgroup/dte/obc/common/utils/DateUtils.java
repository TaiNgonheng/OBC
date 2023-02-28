package com.rhbgroup.dte.obc.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class DateUtils {
    public static OffsetDateTime toOffsetDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atOffset(ZoneOffset.UTC);
    }

    public static Instant toInstant(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return offsetDateTime.toInstant();
    }

    public static Instant toInstant(String dateStr) {
        if (StringUtils.isBlank(dateStr)) return null;
        return Instant.parse(dateStr);
    }

    public static Instant toInstant(Date date) {
        if (date == null) return null;
        return date.toInstant();
    }
}
