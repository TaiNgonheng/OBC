package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;

  @Override
  public void updateBakongId(String username, String bakongId) {

    Optional<UserProfileEntity> userProfileEntity = userProfileRepository.getByUsername(username);
    if (userProfileEntity.isPresent()) {
      UserProfileEntity userProfile = userProfileEntity.get();
      userProfile.setBakongId(bakongId);
      userProfileRepository.save(userProfile);
    }
  }
}
