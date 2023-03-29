package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapperImpl;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final JwtTokenUtils jwtTokenUtils;

  private final ConfigService configService;
  private final UserAuthService userAuthService;

  private final PGRestClient pgRestClient;
  private final InfoBipRestClient infoBipRestClient;

  private final AccountMapper accountMapper = new AccountMapperImpl();

  @Override
  public InitAccountResponse authenticate(InitAccountRequest request) {
    return null;
  }

  @Override
  public InitAccountResponse initLinkAccount(InitAccountRequest request) {

    // Generate OBC token
    String token =
        Functions.of(accountMapper::toModel)
            .andThen(AccountModel::getUser)
            .andThen(userAuthService::authenticate)
            .andThen(jwtTokenUtils::generateJwt)
            .apply(request);

    // Get PG user profile, trigger OTP and build response
    return Functions.of(pgRestClient::getUserProfile)
        .andThen(Functions.peek(AccountValidator::validateAccount))
        .andThen(
            Functions.peek(
                userProfile -> {
                  if (userProfile.getPhone().equals(request.getPhoneNumber()))
                    infoBipRestClient.sendOtp(userProfile.getPhone(), request.getLogin());
                }))
        .andThen(
            profileResponse -> {
              int otpEnabled =
                  configService.getByConfigKey(
                      ConfigConstants.REQUIRED_INIT_ACCOUNT_OTP_KEY,
                      ConfigConstants.VALUE,
                      Integer.class);
              return accountMapper.toInitAccountResponse(
                  request, profileResponse, token, otpEnabled == 1);
            })
        .apply(Collections.singletonList(request.getBakongAccId()));
  }

  @Override
  public VerifyOtpResponse verifyOtp(String authorization, VerifyOtpRequest request) {

    return Functions.of(jwtTokenUtils::extractJwt)
        .andThen(jwtTokenUtils::getUsernameFromJwtToken)
        .andThen(
            loginKey ->
                new VerifyOtpResponse()
                    .status(ResponseHandler.ok())
                    .data(
                        new VerifyOtpResponseAllOfData()
                            .isValid(infoBipRestClient.verifyOtp(request.getOtpCode(), loginKey))))
        .apply(authorization);
  }
}
