package com.rhbgroup.dte.obc.common.constants;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

  @UtilityClass
  public class Role {
    public static final String SYSTEM_USER = "SYSTEM_USER";
    public static final String APP_USER = "APP_USER";
  }

  @UtilityClass
  public class Authentication {
    public static final Integer LOCK_IN_SECOND = 8 * 60 * 60; // 8 hours
    public static final Integer AUTHENTICATION_ALLOWED_TIME = 3;
  }

  @UtilityClass
  public class Permission {
    public static final String CAN_TOP_UP = "can_top_up";
    public static final String CAN_GET_BALANCE = "can_get_balance";
    public static final String CAN_GET_TRANSACTION = "can_get_transaction";
    public static final String CAN_LINK_ACCOUNT = "can_link_account";
    public static final String CAN_UNLINK_ACCOUNT = "can_unlink_account";
    public static final String CAN_AUTH = "can_auth";

    public static String concat(String... permissions) {
      return Arrays.stream(permissions).reduce((s1, s2) -> s1 + "," + s2).orElse(null);
    }
  }

  @UtilityClass
  public class Status {
    public static final int ERROR = 1;
    public static final int SUCCESS = 0;
  }

  @UtilityClass
  public class System {
    public static final String OPEN_BANKING_CLIENT = "OBC_SYSTEM";
    public static final String GOWAVE = "GOWAVE_SYSTEM";
    public static final String OPEN_BANKING_GATEWAY = "NBC_SYSTEM";
    public static final String BAKONG_APP = "BAKONG";
  }

  @UtilityClass
  public class Transaction {
    public static final String OBC_TOP_UP = "OBTOPUP";
    public static final String TRANSACTION_FILE_PREFIX = "OBCDailyTrx_";
    public static final String DATE_FORMAT_DDMMYYYY = "ddMMyyyy";
    public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
    public static final String TRANSACTION_FILE_EXTENSION = ".csv";
    public static final String SIBS_SYNC_DATE_KEY = "SIBS_DATE_CONFIG";
    public static final String RECORD_DETAIL_INDICATOR = "D";
    public static final List<String> CASA_TO_WALLET_CODES = List.of("9186", "9187");
  }
}
