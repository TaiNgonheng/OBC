package com.rhbgroup.dte.obc.domains.user.mapper;

import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserRoleEntity;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

  default UserRoleEntity toEntity(UserProfileEntity userProfile, String role, String permissions) {

    UserRoleEntity userRoleEntity = new UserRoleEntity();
    userRoleEntity.setRole(role);
    userRoleEntity.setPermissions(permissions);

    if (StringUtils.isNotBlank(userProfile.getUsername())) {
      userRoleEntity.setUserId(userProfile.getUsername());

    } else if (StringUtils.isNotBlank(userProfile.getMobileNo())) {
      userRoleEntity.setUserId(userProfile.getMobileNo());
    }

    userRoleEntity.setUpdatedBy(AppConstants.SYSTEM.OPEN_BANKING_CLIENT);
    userRoleEntity.setCreatedDate(Instant.now());

    return userRoleEntity;
  }
}
