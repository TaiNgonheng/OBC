package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapperImpl;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Collections;
import java.util.Optional;
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
  private final UserProfileService userProfileService;

  private final AccountRepository accountRepository;

  private final PGRestClient pgRestClient;
  private final InfoBipRestClient infoBipRestClient;
  private final CDRBRestClient cdrbRestClient;

  private final AccountMapper accountMapper = new AccountMapperImpl();

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    return Functions.of(accountMapper::toUserModel)
        .andThen(userAuthService::authenticate)
        .andThen(
            Functions.peek(
                authContext ->
                    userAuthService.checkUserRole(
                        authContext, Collections.singletonList(AppConstants.ROLE.APP_USER))))
        .andThen(jwtTokenUtils::generateJwt)
        .andThen(accountMapper::toAuthResponse)
        .apply(request);
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
                userProfile ->
                    infoBipRestClient.sendOtp(userProfile.getPhone(), request.getLogin())))
        .andThen(
            Functions.peek(
                response -> insertBakongId(request.getLogin(), request.getBakongAccId())))
        .andThen(
            profileResponse -> {
              Integer otpEnabled =
                  configService.getByConfigKey(
                      ConfigConstants.REQUIRED_INIT_ACCOUNT_OTP_KEY,
                      ConfigConstants.VALUE,
                      Integer.class);

              UserModel gowaveUser =
                  userProfileService.findByUsername(profileResponse.getAccountId());

              return accountMapper.toInitAccountResponse(
                  gowaveUser, profileResponse, token, otpEnabled == 1);
            })
        .apply(Collections.singletonList(request.getBakongAccId()));
  }

  private void insertBakongId(String username, String bakongId) {

    Functions.of(userProfileService::findByUsername)
        .andThen(
            userModel ->
                accountRepository
                    .findByUserIdAndBakongIdAndLinkedStatus(
                        userModel.getId().longValue(), bakongId, AppConstants.LinkStatus.PENDING)
                    .orElseGet(
                        () -> {
                          AccountEntity accountEntity = new AccountEntity();
                          accountEntity.setUserId(userModel.getId().longValue());
                          accountEntity.setBakongId(bakongId);
                          accountEntity.setLinkedStatus(AppConstants.LinkStatus.PENDING);

                          return accountEntity;
                        }))
        .andThen(accountRepository::save)
        .apply(username);
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

  @Override
  public FinishLinkAccountResponse finishLinkAccount(
      String authorization, FinishLinkAccountRequest request) {

    UserModel userProfile =
        userProfileService.findByUsername(
            jwtTokenUtils.getUsernameFromJwtToken(jwtTokenUtils.extractJwt(authorization)));

    accountRepository
        .findByAccountIdAndLinkedStatus(request.getAccNumber(), AppConstants.LinkStatus.COMPLETED)
        .ifPresent(
            account -> {
              throw new BizException(ResponseMessage.ACCOUNT_ALREADY_LINKED);
            });

    return Functions.of(cdrbRestClient::getAccountDetail)
        .andThen(Functions.peek(AccountValidator::validateCasaAccount))
        .andThen(
            account ->
                accountRepository
                    .findByUserIdAndLinkedStatus(
                        userProfile.getId().longValue(), AppConstants.LinkStatus.PENDING)
                    .flatMap(entity -> Optional.of(accountMapper.toAccountEntity(entity, account)))
                    .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND)))
        .andThen(Functions.peek(accountRepository::save))
        .andThen(account -> accountMapper.toFinishLinkAccountResponse())
        .apply(
            authorization,
            new CDRBGetAccountDetailRequest()
                .accountNo(request.getAccNumber())
                .cifNo(userProfile.getCifNo()));
  }

  @Override
  public GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request) {
    return new GetAccountDetailResponse();
  }
}
