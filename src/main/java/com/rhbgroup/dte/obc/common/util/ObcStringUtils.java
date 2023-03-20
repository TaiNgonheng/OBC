package com.rhbgroup.dte.obc.common.util;

import org.apache.commons.lang3.StringUtils;

public class ObcStringUtils extends StringUtils {

  public static String getLast3DigitsPhone(String mobileNumber) {
    if (isBlank(mobileNumber)) {
      return EMPTY;
    }
    return mobileNumber.length() > 3
        ? mobileNumber.substring(mobileNumber.length() - 3)
        : mobileNumber;
  }
}
