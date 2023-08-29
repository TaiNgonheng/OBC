package com.rhbgroup.dte.obc.domains.user.mapper;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.model.UserModel;
import java.util.Optional;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  default Optional<UserModel> toModelOptional(UserProfileEntity entity) {
    return Optional.ofNullable(toModel(entity));
  }

  UserModel toModel(UserProfileEntity entity);

  UserProfileEntity toEntity(UserModel model);
}
