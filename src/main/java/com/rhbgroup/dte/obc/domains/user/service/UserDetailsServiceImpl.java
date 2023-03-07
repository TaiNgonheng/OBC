package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserProfileRepository userProfileRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    UserProfileEntity userProfile =
        userProfileRepository
            .getByUsername(username)
            .orElseThrow(
                () -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority(AppConstants.ROLE.APP_USER));
    authorities.add(new SimpleGrantedAuthority(AppConstants.ROLE.SYSTEM_USER));
    return new User(username, userProfile.getPassword(), authorities);
  }
}
