package com.rhbgroup.dte.obc.security;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtTokenManager {

  @Autowired private UserDetailsService userDetailsService;

  @Autowired private JwtTokenUtils jwtTokenUtils;

  public AuthenticationStatus verifyRequest(HttpServletRequest httpServletRequest) {

    String servletPath = httpServletRequest.getServletPath();
    if (WhitelistUrlManager.isWhitelisted(servletPath)) {
      return AuthenticationStatus.bypassed();
    }

    String authorizationHeader = httpServletRequest.getHeader("Authorization");
    if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.contains("Bearer")) {
      String jwtToken = authorizationHeader.substring(7);

      // TODO if jwt token requires RSA, we need one extra step to validate using public key

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
    String username = jwtTokenUtils.getUsernameFromJwtToken(jwt);
    if (null != username) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }
}
