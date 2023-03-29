package com.rhbgroup.dte.obc.common.constants;

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

    public static final String INFO_BIP_URL_KEY = "INFO_BIP_URL";
    public static final String INFO_BIP_APP_ID_KEY = "INFO_BIP_APP_ID";
    public static final String INFO_BIP_ACCOUNT = "INFO_BIP_ACCOUNT";
    public static final String INFO_BIP_OTP_MESSAGE_ID_KEY = "INFO_BIP_OTP_MESSAGE_ID";
    public static final String INFO_BIP_SEND_OTP_PATH = "/2fa/2/pin";
    public static final String INFO_BIP_VERIFY_OTP_API_PATH = "/2fa/2/pin/{pinId}/verify";
    public static final String INFO_BIP_LOGIN_API_PATH = "/auth/1/oauth2/token";
    public static final String INFO_BIP_SENDER_NAME = "obc";
    public static final String INFO_BIP_CLIENT_ID_KEY = "client_id";
    public static final String INFO_BIP_CLIENT_SECRET_KEY = "client_secret";
    public static final String INFO_BIP_GRANT_TYPE_KEY = "grant_type";
  }
}
