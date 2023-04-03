package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.UserModel;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAuthService {

  private final AuthenticationManager authManager;
  private final UserProfileRepository userProfileRepository;

  public Authentication authenticate(UserModel userModel) {
    UserProfileEntity profile =
        userProfileRepository
            .getByUsername(userModel.getUsername())
            .orElseThrow(
                () -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));
    if (profile.getLogTime() != null && profile.getLogTime().isAfter(Instant.now()))
      throw new UserAuthenticationException(ResponseMessage.AUTHENTICATION_LOCKED);

    try {
      Authentication authentication =
          authManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  userModel.getUsername(), userModel.getPassword()));
      if (profile.getLogAttempt() != null && profile.getLogAttempt() != 0) recordFailAttempt(profile, 0);
      return authentication;
    } catch (AuthenticationException ex) {
      recordFailAttempt(profile, profile.getLogAttempt() + 1);
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

  public String getCurrentUser() {
    try {
      UserDetails principal =
          (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

      return principal.getUsername();
    } catch (Exception ex) {
      throw new BizException(ResponseMessage.SESSION_EXPIRED);
    }
  }

  private void recordFailAttempt(UserProfileEntity profile, Integer attempt) {
    profile.setLogAttempt(attempt);
    if (attempt >= AppConstants.AUTHENTICATION.AUTHENTICATION_ALLOWED_TIME) {
      profile.setLogAttempt(0);
      profile.setLogTime(Instant.now().plusSeconds(AppConstants.AUTHENTICATION.LOCK_SECOND));
    }
    userProfileRepository.save(profile);
  }
}
