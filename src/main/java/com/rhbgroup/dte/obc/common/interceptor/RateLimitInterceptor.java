package com.rhbgroup.dte.obc.common.interceptor;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.exceptions.RateLimitException;
import com.rhbgroup.dte.obc.security.RateLimitSecurityService;
import io.github.bucket4j.Bucket;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Service
public class RateLimitInterceptor implements HandlerInterceptor {

  public RateLimitInterceptor(RateLimitSecurityService rateLimitSecurityService) {
    this.rateLimitSecurityService = rateLimitSecurityService;
  }

  private final RateLimitSecurityService rateLimitSecurityService;

  @Value("${obc.rate-limits.enabled}")
  private boolean isRateLimitsEnabled;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    final String xForwardedHeader = request.getHeader("X-Forwarded-For");
    final String ip =
        xForwardedHeader != null ? xForwardedHeader.split(",")[0] : request.getRemoteAddr();
    log.info("remote address {} and request time {}", ip, new Date());
    if (isRateLimitsEnabled) {
      Bucket bucket = rateLimitSecurityService.resolveBucket(ip);
      log.info("available token {} for key {}", bucket.getAvailableTokens(), ip);
      if (bucket.tryConsume(1)) {
        log.info("remaining token {} for key {}", bucket.getAvailableTokens(), ip);
        return HandlerInterceptor.super.preHandle(request, response, handler);
      } else {
        log.warn("{} Reach rate limited!", ip);
        throw new RateLimitException(ResponseMessage.TOO_MANY_REQUEST_ERROR);
      }
    } else {
      return HandlerInterceptor.super.preHandle(request, response, handler);
    }
  }
}
