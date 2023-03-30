package com.rhbgroup.dte.obc.common.constants.services;

public class ConfigConstants {

  private ConfigConstants() {}

  // Json values
  public static final String VALUE = "value";
  public static final String PASSWORD = "password";
  public static final String USERNAME = "username";
  public static final String AES_KEY = "aesKey";
  public static final String AES_IV = "aesIv";

  // Service Keys
  public static final String REQUIRED_INIT_ACCOUNT_OTP_KEY = "REQUIRED_INIT_ACCOUNT_OTP";

  // PG1 service
  public static final String PG1_ACCOUNT = "PG1_ACCOUNT";
  public static final String PG1_URL = "PG1_URL";

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
  // CDRB service
  public static final String CDRB_URL = "CDRB_URL";
  public static final String CDRB_ACCOUNT = "CDRB_ACCOUNT";
  public static final String CDRB_HSM_ZMK = "CDRB_HSM_ZMK";
  public static final String CDRB_HSM_IV = "CDRB_HSM_IV";
}
