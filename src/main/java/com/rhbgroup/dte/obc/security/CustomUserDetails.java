package com.rhbgroup.dte.obc.security;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import java.util.Collection;
import java.util.Collections;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder(toBuilder = true)
public class CustomUserDetails implements UserDetails {

  private Long userId;
  private Collection<? extends GrantedAuthority> authorities;
  private String permissions;
  private String bakongId;
  private String password;
  private String username;
  private boolean accountNonExpired;
  private boolean accountNonLocked;
  private boolean credentialsNonExpired;
  private boolean enabled;

  public static CustomUserDetails withoutAuthorities(UserProfileEntity userProfile) {
    return CustomUserDetails.builder()
        .userId(userProfile.getId())
        .username(userProfile.getUsername())
        .password(userProfile.getPassword())
        .authorities(Collections.emptySet())
        .accountNonLocked(true)
        .accountNonExpired(true)
        .credentialsNonExpired(true)
        .enabled(true)
        .build();
  }
}
