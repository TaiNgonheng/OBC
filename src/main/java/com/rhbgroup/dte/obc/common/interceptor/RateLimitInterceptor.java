package com.rhbgroup.dte.obc.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rhbgroup.dte.obc.security.RateLimitSecurityService;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

@Slf4j
@Service
public class RateLimitInterceptor implements HandlerInterceptor {

  public RateLimitInterceptor(RateLimitSecurityService rateLimitSecurityService) {
    this.rateLimitSecurityService = rateLimitSecurityService;
  }

  private final RateLimitSecurityService rateLimitSecurityService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    final String xForwardedHeader = request.getHeader("X-Forwarded-For");
    final String ip =
        xForwardedHeader != null ? xForwardedHeader.split(",")[0] : request.getRemoteAddr();
    log.info("remote address {} and request time {}", ip, new Date());

    Bucket bucket = rateLimitSecurityService.resolveBucket(ip);
    log.info("available token {} for key {}", bucket.getAvailableTokens(), ip);
    if (rateLimitSecurityService.tryConsume(bucket, ip)){
      log.info("remaining token {} for key {}", bucket.getAvailableTokens(), ip);
      return HandlerInterceptor.super.preHandle(request, response, handler);
    } else {
       log.warn("Reach rate limited!");
       return false;
    }
  }
}
