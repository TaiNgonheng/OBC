package com.rhbgroup.dte.obc.common.util.impl;

import com.rhbgroup.dte.obc.common.util.CacheUtil;
import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import org.springframework.stereotype.Service;

@Service
public class JcacheUtil implements CacheUtil {

  private Cache<String, String> cache;

  @PostConstruct
  public void postConstruct() {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    CacheManager cacheManager = cachingProvider.getCacheManager();
    MutableConfiguration<String, String> config = new MutableConfiguration<>();
    this.cache = cacheManager.createCache("obcCache", config);
  }

  @Override
  public <T> T getCacheConfig() {
    return null;
  }

  @Override
  public void addKey(String key, String value) {
    this.cache.put(key, value);
  }

  @Override
  public String getValueFromKey(String key) {
    return this.cache.get(key);
  }
}
