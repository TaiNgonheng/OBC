package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.user.mapper.UserProfileMapper;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserProfileMapper userProfileMapper;

  @Override
  public UserModel findByUsername(String username) {

    return userProfileRepository
        .getByUsername(username)
        .flatMap(userProfileMapper::toModelOptional)
        .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
  }

  @Override
  public UserModel findByUserId(Long userId) {
    return userProfileRepository
        .findById(userId)
        .flatMap(userProfileMapper::toModelOptional)
        .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
  }
  private final UserProfileRepository userProfileRepository;

  @Override
  public void addBakongId(String username, String bakongId) {
    Optional<UserProfileEntity> userProfileEntity = userProfileRepository.getByUsername(username);
    if (userProfileEntity.isPresent()) {
      UserProfileEntity userProfile = userProfileEntity.get();
      userProfile.setBakongId(bakongId);
      userProfileRepository.save(userProfile);
    }
  }

  @Override
  public UserProfileEntity findProfileByUserName(String username) {
    return userProfileRepository.getByUsername(username).get();
  }
}
