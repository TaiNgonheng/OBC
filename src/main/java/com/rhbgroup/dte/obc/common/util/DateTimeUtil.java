package com.rhbgroup.dte.obc.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class DateTimeUtil {
  private final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
  private final String DATE_FORMAT_FOR_KEY = "MM-dd-yyyy-HH:mm:ss.SSS";

  public String dateToString(Date dateTime) {
    DateFormat df = new SimpleDateFormat(DATE_FORMAT);
    return df.format(dateTime);
  }

  public String dateToStringFormat(Date dateTime, String dateFormat) {
    DateFormat df = new SimpleDateFormat(dateFormat);
    return df.format(dateTime);
  }

  public String dateToKey(Date dateTime) {
    DateFormat df = new SimpleDateFormat(DATE_FORMAT_FOR_KEY);
    return df.format(dateTime);
  }
}
