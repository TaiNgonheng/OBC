package com.rhbgroup.dte.obc.domains.user.mapper;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.model.ExchangeAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.GWAuthenticationRequest;
import com.rhbgroup.dte.obc.model.GWAuthenticationResponse;
import com.rhbgroup.dte.obc.model.GWAuthenticationResponseAllOfData;
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

  UserModel fromAuthRequestToModel(GWAuthenticationRequest request);

  UserProfileEntity toEntity(UserModel userModel);

  @Named("toInstant")
  default Instant toInstant(OffsetDateTime offsetDateTime) {
    return offsetDateTime == null ? null : offsetDateTime.toInstant();
  }

  default UserExchangeResponse toResponse(ExchangeAccountResponseAllOfData data) {
    return new UserExchangeResponse().status(ResponseHandler.ok()).data(data);
  }

  default GWAuthenticationResponse toAuthResponse(String token) {
    return new GWAuthenticationResponse()
        .status(ResponseHandler.ok())
        .data(new GWAuthenticationResponseAllOfData().token(token));
  }

  default ExchangeAccountResponseAllOfData toGwExchangeUserResponse(Long userId) {
    return new ExchangeAccountResponseAllOfData().userId(String.valueOf(userId));
  }
}
