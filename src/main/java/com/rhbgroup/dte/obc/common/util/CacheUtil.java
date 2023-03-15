package com.rhbgroup.dte.obc.common.util;

import javax.cache.expiry.Duration;

public interface CacheUtil {

  <T> T getCacheConfig();

  void createCache(String cacheName, Duration expireTime);

  void addKey(String key, String value);

  String getValueFromKey(String key);

  void addKey(String cacheName, String key, String value);

  String getValueFromKey(String cacheName, String key);
}
