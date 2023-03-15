package com.rhbgroup.dte.obc.common.util;

public interface CacheUtil {

  <T> T getCacheConfig();

  void addKey(String key, String value);

  String getValueFromKey(String key);
}
