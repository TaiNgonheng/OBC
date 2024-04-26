package com.rhbgroup.dte.obc.common.util;

import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import javax.cache.expiry.Duration;

public interface CacheUtil {

  void createCache(String cacheName, Duration expireTime);

  <T> void createCache(String cacheName, Duration expireTime, Class<T> clazz);

  void addKey(String cacheName, String key, String value);

  String getValueFromKey(String cacheName, String key);

  HazelcastProxyManager<String> getHazelcastProxyManager();
}
