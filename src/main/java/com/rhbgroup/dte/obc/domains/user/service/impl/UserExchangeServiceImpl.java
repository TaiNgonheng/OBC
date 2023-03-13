package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.user.mapper.UserExchangeMapper;
import com.rhbgroup.dte.obc.domains.user.mapper.UserRoleMapper;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.UserRoleRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserRoleEntity;
import com.rhbgroup.dte.obc.domains.user.service.UserExchangeService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.UserModel;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserExchangeServiceImpl implements UserExchangeService {

  private PasswordEncoder passwordEncoder;

  private UserExchangeMapper userExchangeMapper;

  private UserRoleMapper userRoleMapper;

  private UserProfileRepository userProfileRepository;

  private UserRoleRepository userRoleRepository;

  @Override
  @Transactional(rollbackOn = RuntimeException.class)
  public String exchangeUser(UserModel userModel) {

    Functions.of(userExchangeMapper::toEntity)
        .andThen(checkUserExisting)
        .andThen(userProfileRepository::save)
        .andThen(
            userProfileEntity ->
                userRoleMapper.toEntity(
                    userProfileEntity,
                    AppConstants.ROLE.APP_USER,
                    AppConstants.PERMISSION.concat(
                        AppConstants.PERMISSION.CAN_GET_BALANCE,
                        AppConstants.PERMISSION.CAN_TOP_UP)))
        .andThen(Functions.peek(performUserRoleIfNeeded))
        .apply(userModel);

    return "success";
  }

  private final UnaryOperator<UserProfileEntity> checkUserExisting =
      newUser -> {
        Optional<UserProfileEntity> userOptional =
            userProfileRepository.getByUsername(newUser.getUsername());

        // Compare the existing password
        // if the user already exist with username but not password
        // allow to continue to update user password
        if (userOptional.isPresent()) {

          UserProfileEntity existingUser = userOptional.get();
          boolean isMatched =
              passwordEncoder.matches(newUser.getPassword(), existingUser.getPassword());
          if (isMatched) {
            throw new BizException(ResponseMessage.USER_ALREADY_EXIST);
          } else {
            // Update user credential
            existingUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            return existingUser;
          }
        }

        return newUser;
      };

  private final Consumer<UserRoleEntity> performUserRoleIfNeeded =
      userRole -> {
        Optional<UserRoleEntity> byUserId = userRoleRepository.findByUserId(userRole.getUserId());
        if (byUserId.isEmpty()) {
          userRoleRepository.save(userRole);
        }
      };
}
