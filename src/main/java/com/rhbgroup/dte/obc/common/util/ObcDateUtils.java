package com.rhbgroup.dte.obc.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class ObcDateUtils extends org.apache.commons.lang3.time.DateUtils {

  private ObcDateUtils() {}

  public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss";
  public static final String YYYY_MM_DD = "yyyy-MM-dd";
  public static final String YYYY_MM_DD_NO_SPACE = "yyyyMMdd";

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

  public static Instant toInstant(String date, String format) {
    if (StringUtils.isBlank(date)) {
      return null;
    }
    try {
      String controlledFormat = StringUtils.isBlank(format) ? DEFAULT_DATE_FORMAT : format;
      SimpleDateFormat sdf = new SimpleDateFormat(controlledFormat);
      Date parse1 = sdf.parse(date);

      return parse1.toInstant();
    } catch (ParseException ex) {
      return null;
    }
  }

  public static LocalDate toLocalDate(String date, String format) {
    try {
      if (StringUtils.isBlank(date)) {
        return null;
      }
      if (StringUtils.isBlank(format)) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
      }
      return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    } catch (Exception ex) {
      return null;
    }
  }
}
