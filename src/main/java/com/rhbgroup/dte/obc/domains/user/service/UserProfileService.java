package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.model.UserModel;

public interface UserProfileService {

  UserModel findByUsername(String username);

  UserModel findByUserId(Long userId);

  void updateUserProfile(UserModel userModel);
}
