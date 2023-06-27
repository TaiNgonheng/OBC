package com.rhbgroup.dte.obc.domains.account.service.impl;

import static com.rhbgroup.dte.obc.common.func.Functions.of;
import static com.rhbgroup.dte.obc.common.func.Functions.peek;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
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
import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final JwtTokenUtils jwtTokenUtils;

  private final UserAuthService userAuthService;
  private final UserProfileService userProfileService;
  private final ConfigService configService;

  private final AccountRepository accountRepository;

  private final PGRestClient pgRestClient;
  private final InfoBipRestClient infoBipRestClient;
  private final CDRBRestClient cdrbRestClient;

  private final AccountMapper accountMapper = new AccountMapperImpl();

  private final ApplicationProperties properties;

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    validateAuthenticationRequest(request);

    return of(accountMapper::toUserModel)
        .andThen(userAuthService::authenticate)
        .andThen(
            peek(
                authContext -> {
                  // Checking account status
                  CustomUserDetails principal = (CustomUserDetails) authContext.getPrincipal();
                  if (null == principal.getOtpVerified()
                      || Boolean.FALSE.equals(principal.getOtpVerified())) {
                    log.error("User {} is not fully KYC", principal.getUsername());
                    throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
                  }
                  Optional<AccountEntity> activeAccount =
                      accountRepository.findFirstByUserIdAndBakongIdAndLinkedStatus(
                          principal.getUserId(),
                          principal.getBakongId(),
                          LinkedStatusEnum.COMPLETED);
                  if (activeAccount.isEmpty()) {
                    log.error(
                        "No active account found for user {} with bakong id {}",
                        principal.getUserId(),
                        principal.getBakongId());
                    throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
                  }
                }))
        .andThen(
            peek(
                authContext ->
                    userAuthService.checkUserRole(
                        authContext, Collections.singletonList(AppConstants.Role.APP_USER))))
        .andThen(jwtTokenUtils::generateJwtAppUser)
        .andThen(accountMapper::toAuthResponse)
        .apply(request);
  }

  private void validateAuthenticationRequest(AuthenticationRequest request) {
    if (StringUtils.isEmpty(request.getKey())) {
      throw new BizException(ResponseMessage.MISSING_KEY);
    }

    if (!request.getLoginType().equals(LoginTypeEnum.USER_PWD.getValue())
        && !request.getLoginType().equals(LoginTypeEnum.PHONE_PIN.getValue())) {
      throw new BizException(ResponseMessage.OUT_OF_RANGE_LOGIN_TYPE);
    }
  }

  @Override
  public InitAccountResponse initLinkAccount(InitAccountRequest request) {
    validateInitAccountRequest(request);

    // Generate OBC token
    String token =
        of(accountMapper::toModel)
            .andThen(AccountModel::getUser)
            .andThen(userAuthService::authenticate)
            .andThen(
                authContext ->
                    jwtTokenUtils.generateJwtAppUser(request.getBakongAccId(), authContext))
            .apply(request);

    // Get PG user profile, trigger OTP and build response
    return of(pgRestClient::getUserProfile)
        .andThen(peek(AccountValidator::validateAccount))
        .andThen(peek(response -> insertBakongId(request.getLogin(), request.getBakongAccId())))
        .andThen(
            profileResponse -> {
              UserModel gowaveUser = userProfileService.findByUsername(request.getLogin());
              // Trigger infobip 2-fa sms
              if (gowaveUser.getMobileNo().equals(request.getPhoneNumber())
                  && properties.isInitLinkRequiredOtp()) {
                infoBipRestClient.sendOtp(request.getPhoneNumber(), request.getBakongAccId());
              }
              return accountMapper.toInitAccountResponse(
                  gowaveUser, request.getPhoneNumber(), token, properties.isInitLinkRequiredOtp());
            })
        .apply(Collections.singletonList(request.getBakongAccId()));
  }

  private void validateInitAccountRequest(InitAccountRequest request) {
    if (StringUtils.isEmpty(request.getLoginType())) {
      throw new BizException(ResponseMessage.MISSING_LOGIN_TYPE);
    }

    if (!request.getLoginType().equals(LoginTypeEnum.USER_PWD.getValue())) {
      throw new BizException(ResponseMessage.INVALID_LOGIN_TYPE);
    }

    if (StringUtils.isEmpty(request.getLogin())) {
      throw new BizException(ResponseMessage.MISSING_LOGIN);
    }

    if (StringUtils.isEmpty(request.getKey())) {
      throw new BizException(ResponseMessage.MISSING_KEY);
    }

    if (StringUtils.isEmpty(request.getBakongAccId())) {
      throw new BizException(ResponseMessage.MISSING_BAKONG_ACC_ID);
    }

    if (StringUtils.isEmpty(request.getPhoneNumber())) {
      throw new BizException(ResponseMessage.MISSING_PHONE_NUMBER);
    }

    // validate phone number formate
    String regex = "^([[+]8]55)([1-9])(\\d{7,8})$";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(request.getPhoneNumber());
    if (!matcher.find()) {
      throw new BizException(ResponseMessage.INVALID_PHONE_NUMBER);
    }
  }

  private void insertBakongId(String username, String bakongId) {

    of(userProfileService::findByUsername)
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
  public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
    validateVerifyOTPRequest(request);

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    boolean otpVerified =
        infoBipRestClient.verifyOtp(request.getOtpCode(), currentUser.getBakongId());

    if (otpVerified) {
      // Update otp verify status
      of(userProfileService::findByUserId)
          .andThen(
              peek(
                  userProfile -> {
                    userProfile.setOtpVerifiedStatus(true);
                    userProfile.setOtpVerifiedDate(OffsetDateTime.now());
                    userProfileService.updateUserProfile(userProfile);
                  }))
          .apply(currentUser.getUserId());
      return new VerifyOtpResponse()
          .status(ResponseHandler.ok())
          .data(new VerifyOtpResponseAllOfData().isValid(otpVerified));
    } else {
      throw new BizException(ResponseMessage.INVALID_OTP);
    }
  }

  private void validateVerifyOTPRequest(VerifyOtpRequest request) {
    if (StringUtils.isEmpty(request.getOtpCode())) {
      throw new BizException(ResponseMessage.MISSING_OTP_CODE);
    }
  }

  @Override
  public FinishLinkAccountResponse finishLinkAccount(FinishLinkAccountRequest request) {
    validateFinishLinkAccountRequest(request);

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    if (StringUtils.isBlank(currentUser.getBakongId())) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    // One pending at a time
    AccountEntity pendingAccount =
        accountRepository
            .findByUserIdAndBakongIdAndLinkedStatus(
                currentUser.getUserId(), currentUser.getBakongId(), LinkedStatusEnum.PENDING)
            .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    CDRBGetAccountDetailRequest accountDetailRequest =
        new CDRBGetAccountDetailRequest()
            .accountNo(request.getAccNumber())
            .cifNo(userProfileService.findByUserId(currentUser.getUserId()).getCifNo());

    // Validate if acct has been linked already
    accountRepository
        .findByAccountIdAndLinkedStatus(request.getAccNumber(), LinkedStatusEnum.COMPLETED)
        .ifPresent(
            account -> {
              throw new BizException(ResponseMessage.ACCOUNT_ALREADY_LINKED);
            });

    // Get CDRB account detail & update account table
    return of(cdrbRestClient::getAccountDetail)
        .andThen(peek(AccountValidator::validateCasaAccount))
        .andThen(cdrbAccount -> accountMapper.toAccountEntity(pendingAccount, cdrbAccount))
        .andThen(peek(accountRepository::save))
        .andThen(account -> accountMapper.toFinishLinkAccountResponse())
        .apply(accountDetailRequest);
  }

  private void validateFinishLinkAccountRequest(FinishLinkAccountRequest request) {
    if (StringUtils.isEmpty(request.getAccNumber())) {
      throw new BizException(ResponseMessage.MISSING_ACC_NUMBER);
    }
  }

  @Override
  public GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request) {

    validateGetAccountDetailRequest(request);

    UserModel userModel =
        userProfileService.findByUserId(userAuthService.getCurrentUser().getUserId());

    Long accountNumber =
        accountRepository.countByAccountIdAndLinkedStatus(
            request.getAccNumber(), LinkedStatusEnum.COMPLETED);
    if (accountNumber == 0) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    return of(cdrbRestClient::getAccountDetail)
        .andThen(CDRBGetAccountDetailResponse::getAcct)
        .andThen(accountMapper::toAccountDetailResponse)
        .andThen(
            casaAccountResponse -> {
              ConfigService transactionConfig =
                  this.configService.loadJSONValue(
                      ConfigConstants.Transaction.mapCurrency(
                          casaAccountResponse.getData().getAccCcy()));

              return accountMapper.mappingMobileNoAndAccStatus(
                  userModel.getMobileNo(),
                  transactionConfig.getValue(ConfigConstants.Transaction.MIN_AMOUNT, Double.class),
                  transactionConfig.getValue(ConfigConstants.Transaction.MAX_AMOUNT, Double.class),
                  casaAccountResponse);
            })
        .apply(
            new CDRBGetAccountDetailRequest()
                .cifNo(userModel.getCifNo())
                .accountNo(request.getAccNumber()));
  }

  void validateGetAccountDetailRequest(GetAccountDetailRequest request) {
    if (StringUtils.isEmpty(request.getAccNumber())) {
      throw new BizException(ResponseMessage.MISSING_ACC_NUMBER);
    }
  }

  @Override
  public AccountModel getActiveAccount(AccountFilterCondition condition) {

    return accountRepository
        .findByAccountIdAndLinkedStatus(condition.getAccountNo(), LinkedStatusEnum.COMPLETED)
        .map(accountMapper::entityToModel)
        .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));
  }

  @Override
  @Transactional
  public UnlinkAccountResponse unlinkAccount(UnlinkAccountRequest unlinkAccountRequest) {
    validateUnlinkAccountRequest(unlinkAccountRequest);
    AccountEntity accountEntity =
        accountRepository
            .findByAccountIdAndLinkedStatus(
                unlinkAccountRequest.getAccNumber(), LinkedStatusEnum.COMPLETED)
            .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    accountEntity.setLinkedStatus(LinkedStatusEnum.UNLINKED);
    accountRepository.save(accountEntity);

    return new UnlinkAccountResponse().status(ResponseHandler.ok()).data(null);
  }

  private void validateUnlinkAccountRequest(UnlinkAccountRequest unlinkAccountRequest) {
    if (StringUtils.isBlank(unlinkAccountRequest.getAccNumber())) {
      throw new BizException(ResponseMessage.MISSING_ACC_NUMBER);
    }
  }
}
