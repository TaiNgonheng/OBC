package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.model.UserModel;

public interface UserProfileService {

  UserModel findByUsername(String username);

  UserModel findByUserId(Long userId);
}
