package com.rhbgroup.dte.obc.common.util;

import java.util.Random;
import org.apache.commons.lang3.StringUtils;

public class ObcStringUtils extends StringUtils {

  private static final Random RANDOM = new Random();

  public static String getLast3DigitsPhone(String mobileNumber) {
    if (isBlank(mobileNumber)) {
      return EMPTY;
    }
    return mobileNumber.length() > 3
        ? mobileNumber.substring(mobileNumber.length() - 3)
        : mobileNumber;
  }

  public static String randomString(int length) {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'

    return RANDOM
        .ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
