package com.rhbgroup.dte.obc.common.constants;

import java.util.Arrays;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

  @UtilityClass
  public class ROLE {
    public static final String SYSTEM_USER = "SYSTEM_USER";
    public static final String APP_USER = "APP_USER";
  }

  @UtilityClass
  public class AUTHENTICATION {
    public static final Integer LOCK_SECOND = 28800;
    public static final Integer AUTHENTICATION_ALLOWED_TIME = 3;
  }

  @UtilityClass
  public class PERMISSION {
    public static final String CAN_TOP_UP = "can_top_up";
    public static final String CAN_GET_BALANCE = "can_get_balance";
    public static final String CAN_EXCHANGE_USER = "can_exchange_user";

    public static String concat(String... permissions) {
      return Arrays.stream(permissions).reduce((s1, s2) -> s1 + "," + s2).orElse(null);
    }
  }

  @UtilityClass
  public class STATUS {
    public static final int ERROR = 1;
    public static final int SUCCESS = 0;
  }

  @UtilityClass
  public class SYSTEM {
    public static final String OPEN_BANKING_CLIENT = "OBC_SYSTEM";
    public static final String GOWAVE = "GOWAVE_SYSTEM";
    public static final String OPEN_BANKING_GATEWAY = "NBC_SYSTEM";
    public static final String BAKONG_APP = "BAKONG";
  }

  @UtilityClass
  public class LinkStatus {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
  }
}
