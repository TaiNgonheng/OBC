package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.model.UserModel;

public interface UserProfileService {

  void updateBakongId(String username, String bakongId);

  UserModel findByUsername(String username);

  void updateUserStatus(UserModel userProfile, String linked);
}
