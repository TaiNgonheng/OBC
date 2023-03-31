package com.rhbgroup.dte.obc.domains.user.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final AccountRepository accountRepository;

  @Override
  public void updateBakongId(String username, String bakongId) {

    userProfileRepository
        .getByUsername(username)
        .ifPresent(
            userProfile -> {

              // if there is different bakongId
              if (!bakongId.equals(userProfile.getBakongId())) {

                // Verify account has been already linked
                accountRepository
                    .findByUserId(userProfile.getId())
                    .ifPresent(
                        account -> {
                          throw new BizException(ResponseMessage.ACCOUNT_ALREADY_LINKED);
                        });

                userProfile.setBakongId(bakongId);
                userProfileRepository.save(userProfile);
              }
            });
  }
}
