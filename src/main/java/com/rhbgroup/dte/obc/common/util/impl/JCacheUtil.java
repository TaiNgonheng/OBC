package com.rhbgroup.dte.obc.common.util.impl;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.map.IMap;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import javax.annotation.PostConstruct;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import org.springframework.stereotype.Service;

@Service
public class JCacheUtil implements CacheUtil {

  private HazelcastCacheManager cacheManager;
  private HazelcastProxyManager<String> hazelcastProxyManager;

  @PostConstruct
  public void postConstruct() {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    this.cacheManager = (HazelcastCacheManager) cachingProvider.getCacheManager();

    IMap<String, byte[]> iMap =
        this.cacheManager.getHazelcastInstance().getMap(CacheConstants.OBCCache.CACHE_TOKEN_BUCKET);
    hazelcastProxyManager = new HazelcastProxyManager<>(iMap);
  }

  @Override
  public void createCache(String cacheName, Duration expireTime) {
    MutableConfiguration<String, String> config = new MutableConfiguration<>();
    config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(expireTime));
    cacheManager.createCache(cacheName, config);
  }

  @Override
  public <T> void createCache(String cacheName, Duration expireTime, Class<T> clazz) {
    MutableConfiguration<String, T> config = new MutableConfiguration<>();
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
  public void addKey(String cacheName, String key, byte[] value) {
    cacheManager.getCache(cacheName).put(key, value);
  }

  @Override
  public HazelcastProxyManager<String> getHazelcastProxyManager() {
    return this.hazelcastProxyManager;
  }
}
