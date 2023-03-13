package com.rhbgroup.dte.obc.domains.user.mapper;

import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.UserExchangeRequest;
import com.rhbgroup.dte.obc.model.UserExchangeResponse;
import com.rhbgroup.dte.obc.model.UserModel;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserExchangeMapper {

  @Mapping(source = "login", target = "username")
  @Mapping(source = "key", target = "password")
  UserModel toModel(UserExchangeRequest userExchangeRequest);

  @Mapping(source = "otpVerifiedDate", target = "otpVerifiedDate", qualifiedByName = "toInstant")
  UserProfileEntity toEntity(UserModel userModel);

  @Named("toInstant")
  default Instant toInstant(OffsetDateTime offsetDateTime) {
    return offsetDateTime == null ? null : offsetDateTime.toInstant();
  }

  default UserExchangeResponse toResponse(String data) {
    return new UserExchangeResponse()
        .status(new ResponseStatus().code(AppConstants.STATUS.SUCCESS))
        .data(data);
  }
}
