package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;

public interface UserProfileService {

  UserProfileEntity getByUsername(String username);
}
