package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;

public interface UserProfileService {

  void updateBakongId(String username, String bakongId);

  UserProfileEntity getByUsername(String username);
}
