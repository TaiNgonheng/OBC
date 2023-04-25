package com.rhbgroup.dte.obc.common.util.impl;

import com.rhbgroup.dte.obc.common.util.CacheUtil;
import javax.annotation.PostConstruct;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import org.springframework.stereotype.Service;

@Service
public class JCacheUtil implements CacheUtil {

  private CacheManager cacheManager;

  @PostConstruct
  public void postConstruct() {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    this.cacheManager = cachingProvider.getCacheManager();
  }

  @Override
  public void createCache(String cacheName, Duration expireTime) {
    MutableConfiguration<String, String> config = new MutableConfiguration<>();
    config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(expireTime));
    cacheManager.createCache(cacheName, config);
  }

  @Override
  public void addKey(String cacheName, String key, String value) {
    cacheManager.getCache(cacheName).put(key, value);
  }

  @Override
  public String getValueFromKey(String cacheName, String key) {
    return (String) cacheManager.getCache(cacheName).get(key);
  }

  @Override
  public void removeKey(String cacheName, String key) {
    cacheManager.getCache(cacheName).remove(key);
  }
}
