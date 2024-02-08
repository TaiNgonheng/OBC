package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.UserRoleRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserProfileRepository userProfileRepository;
  private final UserRoleRepository userRoleRepository;
  private final AccountRepository accountRepository;

  public UserDetails loadUserByUserIdAndBakongId(Long userId, String bakongId) {

    return userProfileRepository
        .findById(userId)
        .map(userProfile -> withUserPermission(userProfile, bakongId))
        .orElseThrow(() -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    return userProfileRepository
        .getByUsername(username)
        .map(userProfile -> withUserPermission(userProfile, null))
        .orElseThrow(() -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));
  }

  private CustomUserDetails withUserPermission(UserProfileEntity userProfile, String bakongId) {
    return userRoleRepository
        .findByUserId(userProfile.getId())
        .flatMap(
            userRole -> {
              SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole.getRole());
              Set<SimpleGrantedAuthority> authorities = Collections.singleton(authority);
              CustomUserDetails userDetails =
                  CustomUserDetails.builder()
                      .userId(userProfile.getId())
                      .bakongId(bakongId)
                      .username(userProfile.getUsername())
                      .cif(userProfile.getCifNo())
                      .phoneNumber(userProfile.getMobileNo())
                      .permissions(userRole.getPermissions())
                      .password(userProfile.getPassword())
                      .authorities(authorities)
                      .accountNonLocked(userProfile.getLockTime() == null)
                      .accountNonExpired(true)
                      .credentialsNonExpired(true)
                      .enabled(!userProfile.isDeleted())
                      .build();

              return Optional.of(userDetails);
            })
        .orElseGet(() -> CustomUserDetails.withoutAuthorities(userProfile));
  }
}
