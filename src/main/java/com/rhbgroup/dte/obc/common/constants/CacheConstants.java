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
  public class CDRBCache {
    public static final String CACHE_NAME = "CDRB_CACHE";
    public static final String CDRB_LOGIN_KEY = "CDRB_LOGIN";
  }
}
