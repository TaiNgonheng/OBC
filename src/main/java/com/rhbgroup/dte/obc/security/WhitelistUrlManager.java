package com.rhbgroup.dte.obc.security;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class WhitelistUrlManager {

  private static final String[] WHITELIST_URLS = new String[]{"/init-link-account", "/authenticate"};

  private WhitelistUrlManager() {
  }

  public static String[] getWhitelistUrls() {
    return WHITELIST_URLS;
  }

  public static boolean isWhitelisted(String servletPath) {
    if (StringUtils.isBlank(servletPath)) {
      return false;
    }

    return Arrays.asList(WHITELIST_URLS).contains(servletPath);
  }

}
