package com.rhbgroup.dte.obc.common.util.impl;

import com.rhbgroup.dte.obc.common.util.CacheUtil;
import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import org.springframework.stereotype.Service;

@Service
public class JcacheUtil implements CacheUtil {

  private Cache<String, String> cache;

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
  public <T> T getCacheConfig() {
    return null;
  }

  @Override
  public void addKey(String cacheName, String key, String value) {
    Cache<String, String> cache = cacheManager.getCache(cacheName);
    cache.put(key, value);
  }

  public void addKey(String key, String value) {}

  @Override
  public String getValueFromKey(String key) {
    return null;
  }

  @Override
  public String getValueFromKey(String cacheName, String key) {
    Cache<String, String> cache = cacheManager.getCache(cacheName);
    return cache.get(key);
  }
}
