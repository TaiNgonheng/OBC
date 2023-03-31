package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;

  @Override
  public void updateBakongId(String username, String bakongId) {
    // Nothing
  }

  @Override
  public UserProfileEntity getByUsername(String username) {
    return userProfileRepository
        .getByUsername(username)
        .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
  }
}
