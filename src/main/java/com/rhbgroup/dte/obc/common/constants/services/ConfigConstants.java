package com.rhbgroup.dte.obc.common.constants.services;

public class ConfigConstants {

  private ConfigConstants() {}

  // Generic values
  public static final String VALUE = "value";
  public static final String DATA = "data";

  // Service keys
  public static final String REQUIRED_INIT_ACCOUNT_OTP_KEY = "REQUIRED_INIT_ACCOUNT_OTP";

  public static class PGConfig {
    private PGConfig() {}

    public static final String PG1_ACCOUNT_KEY = "PG1_ACCOUNT";
    public static final String PG1_URL_KEY = "PG1_URL";

    // JSON values
    public static final String PG1_DATA_PASSWORD_KEY = "password";
    public static final String PG1_DATA_USERNAME_KEY = "username";
  }

  public static class InfoBip {
    private InfoBip() {}
  }
}
