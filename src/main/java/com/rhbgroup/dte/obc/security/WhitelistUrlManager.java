package com.rhbgroup.dte.obc.security;

import com.rhbgroup.dte.obc.common.constants.AppConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhitelistUrlManager {

  private WhitelistUrlManager() {}

  private static final String[] WHITELIST_URLS =
      new String[] {
        "/init-link-account", "/authenticate", "/gw/login", "/transaction-processing", "/scheduler"
      };

  private static final Map<String, AntMatchers> requestMatchers = new HashMap<>();

  public static String[] getWhitelistUrls() {
    return WHITELIST_URLS;
  }

  public static boolean isWhitelisted(String servletPath) {
    if (StringUtils.isBlank(servletPath)) {
      return false;
    }

    return Arrays.asList(WHITELIST_URLS).contains(servletPath);
  }

  public static String[] getUrls(String channel) {
    if (requestMatchers.isEmpty()) {
      initMatchers();
    }
    AntMatchers gowave = requestMatchers.get(channel);
    List<String> urls = gowave.urls();
    return urls.toArray(new String[0]);
  }

  public static String getRole(String channel) {
    if (requestMatchers.isEmpty()) {
      initMatchers();
    }
    AntMatchers gowave = requestMatchers.get(channel);
    return gowave.role();
  }

  private static void initMatchers() {

    AntMatchers gowaveMatcher =
        new AntMatchers().urls(List.of("/gw/**")).role(AppConstants.Role.SYSTEM_USER);

    AntMatchers nbcMatcher =
        new AntMatchers().urls(List.of("/**")).role(AppConstants.Role.APP_USER);

    requestMatchers.put(AppConstants.System.GOWAVE, gowaveMatcher);
    requestMatchers.put(AppConstants.System.OPEN_BANKING_GATEWAY, nbcMatcher);
  }

  @Getter
  @Setter
  @Accessors(fluent = true)
  public static class AntMatchers {
    private List<String> urls;
    private String role;
  }
}
