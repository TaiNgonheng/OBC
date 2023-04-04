package com.rhbgroup.dte.obc.common.constants;

public class ConfigConstants {

  private ConfigConstants() {}

  public static class InfoBip {
    private InfoBip() {}

    public static final String INFO_BIP_SENDER_NAME = "obc";
    public static final String INFO_BIP_CLIENT_ID_KEY = "client_id";
    public static final String INFO_BIP_CLIENT_SECRET_KEY = "client_secret";
    public static final String INFO_BIP_GRANT_TYPE_KEY = "grant_type";
  }

  public static class Transaction {
    private Transaction() {}

    public static final String CONFIG_KEY = "TRANSACTION_CONFIG";

    public static final String MIN_AMOUNT = "txMinAmt";
    public static final String MAX_AMOUNT = "txMaxAmt";
    public static final String OTP_REQUIRED = "txOtpRequired";
  }
}
