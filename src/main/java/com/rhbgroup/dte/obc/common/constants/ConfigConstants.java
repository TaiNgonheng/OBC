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

    public static final String CONFIG_KEY_USD = "TRANSACTION_CONFIG_USD";
    public static final String CONFIG_KEY_KHR = "TRANSACTION_CONFIG_KHR";
    public static final String TRX_QUERY_MAX_DURATION = "TRX_QUERY_MAX_DURATION";

    public static final String MIN_AMOUNT = "txMinAmt";
    public static final String MAX_AMOUNT = "txMaxAmt";
    public static final String OTP_REQUIRED = "txOtpRequired";

    public static String mapCurrency(String currencyCode) {
      if ("USD".equalsIgnoreCase(currencyCode)) {
        return CONFIG_KEY_USD;
      }
      // More currency mapping will be put here
      return CONFIG_KEY_KHR;
    }
  }
}
