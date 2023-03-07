package com.rhbgroup.dte.obc.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

  @UtilityClass
  public class ROLE {
    public static final String SYSTEM_USER = "SYSTEM_USER";
    public static final String APP_USER = "APP_USER";
  }

  @UtilityClass
  public class STATUS {
    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
  }
}
