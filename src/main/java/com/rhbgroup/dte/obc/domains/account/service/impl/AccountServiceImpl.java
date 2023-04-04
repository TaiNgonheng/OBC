package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapperImpl;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
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
import com.rhbgroup.dte.obc.model.UnlinkAccountRequest;
import com.rhbgroup.dte.obc.model.UnlinkAccountResponse;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final JwtTokenUtils jwtTokenUtils;
  private final UserAuthService userAuthService;
  private final UserProfileService userProfileService;
  private final AccountRepository accountRepository;
  private final PGRestClient pgRestClient;
  private final InfoBipRestClient infoBipRestClient;
  private final CDRBRestClient cdrbRestClient;
  private final AccountMapper accountMapper = new AccountMapperImpl();

  @Value("${obc.infobip.enabled}")
  protected boolean otpEnabled;

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    return Functions.of(accountMapper::toUserModel)
        .andThen(userAuthService::authenticate)
        .andThen(
            Functions.peek(
                authContext -> {
                  // Checking account status
                  CustomUserDetails principal = (CustomUserDetails) authContext.getPrincipal();
                  AccountEntity accountEntity =
                      accountRepository
                          .findFirstByUserIdAndBakongIdAndLinkedStatus(
                              principal.getUserId(),
                              principal.getBakongId(),
                              LinkedStatusEnum.COMPLETED)
                          .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

                  log.info("Found account entity >> {}", accountEntity);
                }))
        .andThen(
            Functions.peek(
                authContext ->
                    userAuthService.checkUserRole(
                        authContext, Collections.singletonList(AppConstants.Role.APP_USER))))
        .andThen(jwtTokenUtils::generateJwtAppUser)
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
            .andThen(
                authContext ->
                    jwtTokenUtils.generateJwtAppUser(request.getBakongAccId(), authContext))
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
              UserModel gowaveUser = userProfileService.findByUsername(request.getLogin());

              return accountMapper.toInitAccountResponse(
                  gowaveUser, profileResponse, token, otpEnabled);
            })
        .apply(Collections.singletonList(request.getBakongAccId()));
  }

  private void insertBakongId(String username, String bakongId) {

    Functions.of(userProfileService::findByUsername)
        .andThen(
            userModel ->
                accountRepository
                    .findByUserIdAndBakongIdAndLinkedStatus(
                        userModel.getId().longValue(), bakongId, LinkedStatusEnum.PENDING)
                    .orElseGet(
                        () -> {
                          AccountEntity accountEntity = new AccountEntity();
                          accountEntity.setUserId(userModel.getId().longValue());
                          accountEntity.setBakongId(bakongId);
                          accountEntity.setLinkedStatus(LinkedStatusEnum.PENDING);

                          return accountEntity;
                        }))
        .andThen(accountRepository::save)
        .apply(username);
  }

  @Override
  public VerifyOtpResponse verifyOtp(String authorization, VerifyOtpRequest request) {

    return Functions.of(jwtTokenUtils::getSubject)
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

    String bakongId = jwtTokenUtils.getSubject(authorization);
    Long userId = Long.parseLong(jwtTokenUtils.getUserId(authorization));

    if (StringUtils.isBlank(bakongId)) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    // One pending at a time
    AccountEntity pendingAccount =
        accountRepository
            .findByUserIdAndBakongIdAndLinkedStatus(userId, bakongId, LinkedStatusEnum.PENDING)
            .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    CDRBGetAccountDetailRequest accountDetailRequest =
        new CDRBGetAccountDetailRequest()
            .accountNo(request.getAccNumber())
            .cifNo(userProfileService.findByUserId(userId).getCifNo());

    // Validate if acct has been linked already
    accountRepository
        .findByAccountIdAndLinkedStatus(request.getAccNumber(), LinkedStatusEnum.COMPLETED)
        .ifPresent(
            account -> {
              throw new BizException(ResponseMessage.ACCOUNT_ALREADY_LINKED);
            });

    // Get CDRB account detail & update account table
    return Functions.of(cdrbRestClient::getAccountDetail)
        .andThen(Functions.peek(AccountValidator::validateCasaAccount))
        .andThen(cdrbAccount -> accountMapper.toAccountEntity(pendingAccount, cdrbAccount))
        .andThen(Functions.peek(accountRepository::save))
        .andThen(account -> accountMapper.toFinishLinkAccountResponse())
        .apply(accountDetailRequest);
  }

  @Override
  public GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request) {
    return new GetAccountDetailResponse();
  }

  @Override
  public UnlinkAccountResponse unlinkAccount(
      String authorization, UnlinkAccountRequest unlinkAccountRequest) {
    return Functions.of(jwtTokenUtils::extractJwt)
        .andThen(jwtTokenUtils::getUserId)
        .andThen(
            Functions.peek(
                userId -> {
                  AccountEntity accountEntity =
                      accountRepository.getByAccountIdAndUserId(
                          unlinkAccountRequest.getAccNumber(), userId);
                  if (accountEntity != null) {
                    accountEntity.setAccountStatus(AccountStatusEnum.DEACTIVATED.getStatus());
                    accountRepository.save(accountEntity);
                  }
                }))
        .andThen(
            userProfileEntity ->
                new UnlinkAccountResponse().status(ResponseHandler.ok()).data(null))
        .apply(authorization);
  }
}
