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

  public UserDetails loadUserByUserId(Long userId, String bakongId) {

    return userProfileRepository
        .findById(userId)
        .map(
            user ->
                userRoleRepository
                    .findByUserId(user.getId())
                    .flatMap(
                        userRole -> {
                          SimpleGrantedAuthority authority =
                              new SimpleGrantedAuthority(userRole.getRole());
                          Set<SimpleGrantedAuthority> authorities =
                              Collections.singleton(authority);
                          return Optional.of(
                              CustomUserDetails.builder()
                                  .userId(user.getId())
                                  .username(user.getUsername())
                                  .bakongId(bakongId)
                                  .permissions(userRole.getPermissions())
                                  .password(user.getPassword())
                                  .authorities(authorities)
                                  .accountNonLocked(true)
                                  .accountNonExpired(true)
                                  .credentialsNonExpired(true)
                                  .enabled(true)
                                  .build());
                        })
                    .orElseGet(() -> CustomUserDetails.withoutAuthorities(user)))
        .orElseThrow(() -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    UserProfileEntity userProfile =
        userProfileRepository
            .getByUsername(username)
            .orElseThrow(
                () -> new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    return userRoleRepository
        .findByUserId(userProfile.getId())
        .flatMap(
            userRole -> {
              SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole.getRole());
              Set<SimpleGrantedAuthority> singleton = Collections.singleton(authority);
              CustomUserDetails userDetails =
                  CustomUserDetails.builder()
                      .userId(userProfile.getId())
                      .username(username)
                      .permissions(userRole.getPermissions())
                      .password(userProfile.getPassword())
                      .authorities(singleton)
                      .accountNonLocked(true)
                      .accountNonExpired(true)
                      .credentialsNonExpired(true)
                      .enabled(true)
                      .build();

              return Optional.of(userDetails);
            })
        .flatMap(
            userDetail -> {
              CustomUserDetails customUserDetails =
                  accountRepository
                      .findByUserId(userProfile.getId())
                      .flatMap(
                          account ->
                              Optional.of(
                                  userDetail.toBuilder().bakongId(account.getBakongId()).build()))
                      .orElseGet(() -> userDetail);

              return Optional.of(customUserDetails);
            })
        .orElseGet(() -> CustomUserDetails.withoutAuthorities(userProfile));
  }
}
