package com.rhbgroup.dte.obc.domains.user.mapper;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.model.UserModel;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  default Optional<UserModel> toModelOptional(UserProfileEntity entity) {
    return Optional.ofNullable(toModel(entity));
  }

  @Mapping(
      source = "otpVerifiedDate",
      target = "otpVerifiedDate",
      qualifiedByName = "toOffsetDateTime")
  UserModel toModel(UserProfileEntity entity);

  @Mapping(source = "otpVerifiedDate", target = "otpVerifiedDate", qualifiedByName = "toInstant")
  UserProfileEntity toEntity(UserModel model);

  @Named("toOffsetDateTime")
  default OffsetDateTime toOffsetDateTime(Instant instant) {
    return null == instant ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

  @Named("toInstant")
  default Instant toInstant(OffsetDateTime offsetDateTime) {
    return null == offsetDateTime ? null : offsetDateTime.toInstant();
  }
}
