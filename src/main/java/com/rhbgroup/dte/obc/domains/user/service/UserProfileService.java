package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.model.UserModel;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;

public interface UserProfileService {

  UserModel findByUsername(String username);

  UserModel findByUserId(Long userId);
  void addBakongId(String username, String bakongId);

  UserProfileEntity findProfileByUserName(String username);
}
