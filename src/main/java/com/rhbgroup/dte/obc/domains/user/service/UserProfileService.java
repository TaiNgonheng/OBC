package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;

public interface UserProfileService {
  void addBakongId(String username, String bakongId);

  UserProfileEntity findProfileByUserName(String username);
}
