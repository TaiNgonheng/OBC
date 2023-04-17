package com.rhbgroup.dte.obc.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class ObcDateUtils extends org.apache.commons.lang3.time.DateUtils {

  private ObcDateUtils() {}

  private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss";

  public static String toDateString(Date date, String format) {
    if (date == null) {
      return StringUtils.EMPTY;
    }

    if (StringUtils.isBlank(format)) {
      SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
      return df.format(date);
    }

    SimpleDateFormat df = new SimpleDateFormat(format);
    return df.format(date);
  }
}
