package com.rhbgroup.dte.obc.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheConstants {

  @UtilityClass
  public class PGCache {
    public static final String CACHE_NAME = "PG1_CACHE";
    public static final String PG1_LOGIN_KEY = "PG1_LOGIN";
  }

  @UtilityClass
  public class InfoBipCache {
    public static final String CACHE_NAME = "INFO_BIP_CACHE";
    public static final String PIN_ID_KEY = "INFO_BIP_PIN_ID";
    public static final String INFOBIP_LOGIN_KEY = "INFO_BIP_LOGIN";
  }

  @UtilityClass
  public class CDRBCache {
    public static final String CACHE_NAME = "CDRB_CACHE";
    public static final String CDRB_LOGIN_KEY = "CDRB_LOGIN";
  }
}
