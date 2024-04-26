package com.rhbgroup.dte.obc.security;

import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;

import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.LockFreeBucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimitSecurityService {

  private final CacheUtil cacheUtil;
  public RateLimitSecurityService(CacheUtil cacheUtil) {
    this.cacheUtil = cacheUtil;
  }

  @PostConstruct
  public void initCache() {
      cacheUtil.createByteCache(CacheConstants.OBCCache.CACHE_TOKEN_BUCKET, new Duration(TimeUnit.MINUTES, 10));
  }

  public Bucket resolveBucket(String key) throws IOException {
    byte[] snapshotBucket = cacheUtil.getByteValueFromKey(CacheConstants.OBCCache.CACHE_TOKEN_BUCKET, key);
    Bucket bucket;
    if (snapshotBucket == null || snapshotBucket.length == 0) {
      log.info("no available bucket in cache {}", key);
      bucket = newBucket();
      cacheUtil.addKey(CacheConstants.OBCCache.CACHE_TOKEN_BUCKET, key, ((LocalBucket)bucket).toBinarySnapshot());
    } else {
      log.info("available bucket in cache {}", key);
      bucket = LocalBucket.fromBinarySnapshot(snapshotBucket);
    }
    return bucket;
  }

  public boolean tryConsume(Bucket bucket, String key) throws IOException {
    if (bucket.tryConsume(1)) {
      cacheUtil.addKey(CacheConstants.OBCCache.CACHE_TOKEN_BUCKET, key, ((LocalBucket)bucket).toBinarySnapshot());
      return true;
    }
    return false;
  }

  private Bucket newBucket() {
    Bandwidth limit = Bandwidth.simple(5, java.time.Duration.ofMinutes(1));
    return Bucket.builder()
            .addLimit(limit)
            .build();
  }
  @Scheduled(fixedDelay = 5000)
  public void bucketTracker() throws IOException {
    byte[] snapshotBucket = cacheUtil.getByteValueFromKey(CacheConstants.OBCCache.CACHE_TOKEN_BUCKET, "127.0.0.1");
    if (snapshotBucket != null && snapshotBucket.length > 0) {
      Bucket bucket = LocalBucket.fromBinarySnapshot(snapshotBucket);
      log.info("Current time is {} and token in bucket {}", new Date(), bucket.getAvailableTokens());
    } else {
      log.info("{} No bucket!", new Date());
    }
  }
}
