package com.rhbgroup.dte.obc.security;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.domains.user.service.UserDetailsServiceImpl;
import com.rhbgroup.dte.obc.exceptions.BizException;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenManager {

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtTokenUtils jwtTokenUtils;

  private final CacheUtil cacheUtil;

  public AuthenticationStatus verifyRequest(HttpServletRequest httpServletRequest) {

    String servletPath = httpServletRequest.getServletPath();
    if (WhitelistUrlManager.isWhitelisted(servletPath)) {
      return AuthenticationStatus.bypassed();
    }

    String authorizationHeader = httpServletRequest.getHeader("Authorization");
    if (StringUtils.isNotBlank(authorizationHeader)) {
      String jwtToken = jwtTokenUtils.extractJwt(authorizationHeader);

      // Verify if token is valid
      if (jwtTokenUtils.notValidFormat(jwtToken)) {
        return AuthenticationStatus.invalid();
      }

      // Verify if token is not expired
      if (jwtTokenUtils.isExpired(jwtToken)) {
        return AuthenticationStatus.expired();
      }

      return AuthenticationStatus.success(jwtToken);
    }

    return AuthenticationStatus.bypassed();
  }

  public void supplySecurityContext(HttpServletRequest request, String jwt) {

    String appName = request.getHeader("App-Name");
    UserDetails userDetails;

    try {
      if (AppConstants.System.BAKONG_APP.equals(appName)) {
        // APP_USER
        Long userId = Long.parseLong(jwtTokenUtils.getSubject(jwt));
        String bakongId = cacheUtil.getValueFromKey(CacheConstants.OBCCache.CACHE_NAME, jwt);
        userDetails = userDetailsService.loadUserByUserIdAndBakongId(userId, bakongId);
      } else {
        // SYSTEM_USER
        String username = jwtTokenUtils.getSubject(jwt);
        userDetails = userDetailsService.loadUserByUsername(username);
      }

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);

    } catch (Exception ex) {
      throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
    }
  }
}
