package com.rhbgroup.dte.obc.common.constants;

public class ConfigConstants {

  private ConfigConstants() {}

  // Generic values
  public static final String VALUE = "value";
  public static final String REQUIRED_INIT_ACCOUNT_OTP_KEY = "REQUIRED_INIT_ACCOUNT_OTP";

  public static class InfoBip {
    private InfoBip() {}

    public static final String INFO_BIP_SENDER_NAME = "obc";
    public static final String INFO_BIP_CLIENT_ID_KEY = "client_id";
    public static final String INFO_BIP_CLIENT_SECRET_KEY = "client_secret";
    public static final String INFO_BIP_GRANT_TYPE_KEY = "grant_type";
  }
}
