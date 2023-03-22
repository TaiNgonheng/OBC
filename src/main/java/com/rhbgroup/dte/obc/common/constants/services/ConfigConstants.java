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

  public static class CDRB {
    private CDRB() {}

    public static final String CDRB_CREDENTIAL_KEY = "CDRB_ACCOUNT";
    public static final String CDRB_URL_KEY = "CDRB_URL";
    public static final String CDRB_GET_HSM_KEY_PATH = "/auth/hsm-key";
    public static final String CDRB_API_KEY = "x-api-key";
    public static final String CDRB_API_KEY_VALUE = "NQrIN7HPBt141uX5yw2SZ4NigpagyHkZ8cG9b2rf";
  }
}
