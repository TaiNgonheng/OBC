package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.user.mapper.UserExchangeMapper;
import com.rhbgroup.dte.obc.domains.user.mapper.UserRoleMapper;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.UserRoleRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserRoleEntity;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.domains.user.service.UserExchangeService;
import com.rhbgroup.dte.obc.model.ExchangeAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserExchangeServiceImpl implements UserExchangeService {

  private PasswordEncoder passwordEncoder;

  private UserExchangeMapper userExchangeMapper;

  private UserRoleMapper userRoleMapper;

  private UserProfileRepository userProfileRepository;

  private UserRoleRepository userRoleRepository;

  private UserAuthService userAuthService;

  private JwtTokenUtils jwtTokenUtils;

  @Override
  @Transactional(rollbackOn = RuntimeException.class)
  public ExchangeAccountResponseAllOfData exchangeUser(UserModel userModel) {

    return Functions.of(userExchangeMapper::toEntity)
        .andThen(getNewUserOrUpdatedUser)
        .andThen(userProfileRepository::save)
        .andThen(
            userProfileEntity ->
                userRoleMapper.toEntity(
                    userProfileEntity,
                    AppConstants.ROLE.APP_USER,
                    AppConstants.PERMISSION.concat(
                        AppConstants.PERMISSION.CAN_GET_BALANCE,
                        AppConstants.PERMISSION.CAN_TOP_UP)))
        .andThen(performUserRoleIfNeeded)
        .andThen(userExchangeMapper::toGwExchangeUserResponse)
        .apply(userModel);
  }

  @Override
  public String revokeToken(UserModel model) {

    return Functions.of(userAuthService::authenticate)
        .andThen(jwtTokenUtils::generateJwt)
        .apply(model);
  }

  private final UnaryOperator<UserProfileEntity> getNewUserOrUpdatedUser =
      newUser -> {
        Optional<UserProfileEntity> userOptional =
            userProfileRepository.getByUsername(newUser.getUsername());

        if (userOptional.isPresent()) {
          UserProfileEntity existingUser = userOptional.get();
          // Update user credential & mobile number
          String newPassword = newUser.getPassword();
          String newMobileNo = newUser.getMobileNo();
          if (StringUtils.isNotBlank(newPassword)) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
          }
          if (StringUtils.isNotBlank(newMobileNo)) {
            existingUser.setMobileNo(newMobileNo);
          }
          return existingUser;
        }
        // Update user credential
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        return newUser;
      };

  private final Function<UserRoleEntity, Long> performUserRoleIfNeeded =
      newUserRole -> {
        userRoleRepository
            .findByUserId(newUserRole.getUserId())
            .ifPresentOrElse(
                entity -> log.info("userRole::Found existing entity >> {}", entity),
                () -> userRoleRepository.save(newUserRole));
        return newUserRole.getUserId();
      };
}
