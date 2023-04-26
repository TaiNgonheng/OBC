package com.rhbgroup.dte.obc.common.util;

import javax.cache.expiry.Duration;

public interface CacheUtil {

  void createCache(String cacheName, Duration expireTime);

  void addKey(String cacheName, String key, String value);

  String getValueFromKey(String cacheName, String key);

  void removeKey(String cacheName, String key);

  boolean isEmptyCache(String cacheName);
}
