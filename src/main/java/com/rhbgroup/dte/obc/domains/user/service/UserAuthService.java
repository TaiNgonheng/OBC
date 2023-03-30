package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.UserModel;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAuthService {

  private final AuthenticationManager authManager;

  public Authentication authenticate(UserModel userModel) {
    try {
      return authManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              userModel.getUsername(), userModel.getPassword()));

    } catch (AuthenticationException ex) {
      throw new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED);
    }
  }

  public void checkUserRole(Authentication authentication, List<String> roles) {

    int matchCount = 0;
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    for (GrantedAuthority authority : authorities) {
      if (roles.contains(authority.getAuthority())) {
        matchCount++;
      }
    }

    if (matchCount != authorities.size()) {
      throw new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED);
    }
  }
}
