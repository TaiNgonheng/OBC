package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.UserRoleRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserRoleEntity;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import java.util.Collections;
import java.util.Optional;
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

  private final UserRoleRepository userRoleRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    UserProfileEntity userProfile =
        userProfileRepository
            .getByUsername(username)
            .orElseThrow(
                () -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    Optional<UserRoleEntity> byUserId = userRoleRepository.findByUserId(userProfile.getId());
    if (byUserId.isPresent()) {
      UserRoleEntity userRoleEntity = byUserId.get();
      SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRoleEntity.getRole());
      Set<SimpleGrantedAuthority> singleton = Collections.singleton(authority);
      return new User(username, userProfile.getPassword(), singleton);
    }

    return new User(username, userProfile.getPassword(), Collections.emptySet());
  }
}
