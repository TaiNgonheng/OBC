package com.rhbgroup.dte.obc.common.util;

import javax.cache.expiry.Duration;

public interface CacheUtil {

  void createCache(String cacheName, Duration expireTime);

  <T> void createCache(String cacheName, Duration expireTime, Class<T> clazz);

  void createByteCache(String cacheName, Duration expireTime);

  void addKey(String cacheName, String key, String value);

  String getValueFromKey(String cacheName, String key);

  void addKey(String cacheName, String key, Object obj);

  void addKey(String cacheName, String key, byte[] value);

  <T> T getValueFromKey(String cacheName, String key, Class<T> clazz);

  byte[] getByteValueFromKey(String cacheName, String key);
}
