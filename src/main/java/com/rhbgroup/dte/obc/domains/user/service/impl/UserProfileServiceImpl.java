package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.user.mapper.UserProfileMapper;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.UserModel;
import java.time.Instant;
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

  @Override
  public void updateUserProfile(UserModel userModel) {
    Functions.of(userProfileMapper::toEntity)
        .andThen(
            entity -> {
              entity.setUpdatedBy(userModel.getUsername());
              entity.setUpdatedDate(Instant.now());
              return entity;
            })
        .andThen(userProfileRepository::save)
        .apply(userModel);
  }
}
