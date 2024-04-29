package com.rhbgroup.dte.obc.security;

import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import io.github.bucket4j.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RateLimitSecurityService {

  private final CacheUtil cacheUtil;

  @Value("${obc.rate-limits.bandwidths.capacity}")
  private long rateLimitsBandWidthsCapacity;

  @Value("${obc.rate-limits.bandwidths.period}")
  private long rateLimitsBandWidthsPeriod;

  public RateLimitSecurityService(CacheUtil cacheUtil) {
    this.cacheUtil = cacheUtil;
  }

  @PostConstruct
  public void initCache() {
    cacheUtil.createCache(
        CacheConstants.OBCCache.CACHE_TOKEN_BUCKET,
        new Duration(TimeUnit.MINUTES, 10),
        byte[].class);
  }

  public Bucket resolveBucket(String key) {
    return cacheUtil
        .getHazelcastProxyManager()
        .builder()
        .build(key, this::rateLimiterAnnotationsToBucketConfiguration);
  }

  private BucketConfiguration rateLimiterAnnotationsToBucketConfiguration() {
    ConfigurationBuilder configBuilder = new ConfigurationBuilder();
    Bandwidth limit =
        Bandwidth.classic(
            rateLimitsBandWidthsCapacity,
            Refill.intervally(
                rateLimitsBandWidthsCapacity,
                java.time.Duration.ofMinutes(rateLimitsBandWidthsPeriod)));
    configBuilder.addLimit(limit);
    return configBuilder.build();
  }
}
