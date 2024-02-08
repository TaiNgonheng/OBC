package com.rhbgroup.dte.obc.domains.account.service.impl;

import static com.rhbgroup.dte.obc.common.func.Functions.of;
import static com.rhbgroup.dte.obc.common.func.Functions.peek;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
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
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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

  private final CacheUtil cacheUtil;

  @Value("${obc.security.jwt-ttl}")
  protected long tokenTTL; // in second

  @PostConstruct
  private void initCache() {
    cacheUtil.createCache(
        CacheConstants.OBCCache.CACHE_NAME, new Duration(TimeUnit.MILLISECONDS, tokenTTL * 1000));
  }

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
                  boolean activeAccountExisted =
                      accountRepository.existsByUserIdAndLinkedStatus(
                          principal.getUserId(), LinkedStatusEnum.COMPLETED);
                  if (!activeAccountExisted) {
                    log.error("No active account found for user {}", principal.getUserId());
                    throw new BizException(ResponseMessage.ACC_NOT_LINKED);
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
    if (StringUtils.isEmpty(request.getLoginType())) {
      throw new BizException(ResponseMessage.MISSING_LOGIN_TYPE);
    }

    if (!request.getLoginType().equals(LoginTypeEnum.USER_PWD.getValue())
        && !request.getLoginType().equals(LoginTypeEnum.PHONE_PIN.getValue())) {
      throw new BizException(ResponseMessage.OUT_OF_RANGE_LOGIN_TYPE);
    }

    if (StringUtils.isEmpty(request.getLogin())) {
      throw new BizException(ResponseMessage.MISSING_LOGIN);
    }

    if (StringUtils.isEmpty(request.getKey())) {
      throw new BizException(ResponseMessage.MISSING_KEY);
    } else {
      validateKey(request.getKey());
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
            .andThen(peek(this::unlinkObsoleteLinkedToDeactivatedBakongIdRecords))
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
                infoBipRestClient.sendOtp(request.getPhoneNumber(), gowaveUser.getId().toString());
              }

              cacheUtil.addKey(CacheConstants.OBCCache.CACHE_NAME, token, request.getBakongAccId());

              return accountMapper.toInitAccountResponse(
                  gowaveUser, request.getPhoneNumber(), token, properties.isInitLinkRequiredOtp());
            })
        .apply(Collections.singletonList(request.getBakongAccId()));
  }

  private void unlinkObsoleteLinkedToDeactivatedBakongIdRecords(Authentication authContext) {
    CustomUserDetails userDetails = (CustomUserDetails) authContext.getPrincipal();
    Long userId = userDetails.getUserId();
    String userBakongId = userDetails.getBakongId();

    List<AccountEntity> linkedAccounts =
        accountRepository.findByUserIdAndNotTheBakongIdAndLinkedStatus(
            userId, userBakongId, LinkedStatusEnum.COMPLETED);

    List<AccountEntity> accountsToUnlink =
        linkedAccounts.stream().filter(this::isBakongIdDeactivated).collect(Collectors.toList());

    accountsToUnlink.forEach(account -> account.setLinkedStatus(LinkedStatusEnum.UNLINKED));

    accountRepository.saveAll(accountsToUnlink);
  }

  private boolean isBakongIdDeactivated(AccountEntity accountEntity) {
    PGProfileResponse bakongProfile =
        pgRestClient.getUserProfile(Collections.singletonList(accountEntity.getBakongId()));

    return AccountStatusEnum.parse(bakongProfile.getAccountStatus())
        == AccountStatusEnum.DEACTIVATED;
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
    } else {
      validateKey(request.getKey());
    }

    if (StringUtils.isEmpty(request.getBakongAccId())) {
      throw new BizException(ResponseMessage.MISSING_BAKONG_ACC_ID);
    }

    if (StringUtils.isEmpty(request.getPhoneNumber())) {
      throw new BizException(ResponseMessage.MISSING_PHONE_NUMBER);
    }

    // validate phone number formate
    String phoneRegex = "^([[+]8]55)([1-9])(\\d{7,8})$";

    Pattern pattern = Pattern.compile(phoneRegex);
    Matcher matcher = pattern.matcher(request.getPhoneNumber());
    if (!matcher.find()) {
      throw new BizException(ResponseMessage.INVALID_PHONE_NUMBER);
    }
  }

  private void validateKey(String key) {
    String pwdRegex = "^[a-f0-9]{64}$";

    Pattern pwdPattern = Pattern.compile(pwdRegex);
    Matcher pwdMatcher = pwdPattern.matcher(key);
    if (!pwdMatcher.find()) {
      throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
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
                          accountEntity.setOtpVerified(false);
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
        infoBipRestClient.verifyOtp(request.getOtpCode(), currentUser.getUserId().toString());
    if (otpVerified) {
      // Update otp verify status
      of(this::findByUserIdAndBakongIdAndLinkedStatus)
          .andThen(
              peek(
                  account -> {
                    account.setOtpVerified(true);
                    account.setOtpVerifiedDateTime(Instant.now());
                    accountRepository.save(account);
                  }))
          .apply(currentUser);
      return new VerifyOtpResponse()
          .status(ResponseHandler.ok())
          .data(new VerifyOtpResponseAllOfData().isValid(true));
    } else {
      throw new BizException(ResponseMessage.INVALID_OTP);
    }
  }

  private AccountEntity findByUserIdAndBakongIdAndLinkedStatus(CustomUserDetails currentUser) {
    return accountRepository
        .findByUserIdAndBakongIdAndLinkedStatus(
            currentUser.getUserId(), currentUser.getBakongId(), LinkedStatusEnum.PENDING)
        .orElseThrow(() -> new BizException(ResponseMessage.INVALID_TOKEN));
  }

  private void validateVerifyOTPRequest(VerifyOtpRequest request) {
    if (!properties.isInitLinkRequiredOtp()) {
      throw new UserAuthenticationException(ResponseMessage.INVALID_TOKEN);
    }

    if (StringUtils.isEmpty(request.getOtpCode())) {
      throw new BizException(ResponseMessage.MISSING_OTP_CODE);
    }
  }

  @Override
  @Transactional
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
            .orElseThrow(() -> new UserAuthenticationException(ResponseMessage.INVALID_TOKEN));
    UserModel user = userProfileService.findByUserId(currentUser.getUserId());

    CDRBGetAccountDetailRequest accountDetailRequest =
        new CDRBGetAccountDetailRequest().accountNo(request.getAccNumber()).cifNo(user.getCifNo());

    Optional<AccountEntity> byAccountIdAndLinkedStatusCompleted =
        accountRepository.findByAccountIdAndLinkedStatus(
            request.getAccNumber(), LinkedStatusEnum.COMPLETED);

    if (byAccountIdAndLinkedStatusCompleted.isPresent()) {
      log.info(">>>>>> checking linking status");
      AccountEntity previousLinkedAccount = byAccountIdAndLinkedStatusCompleted.get();
      if (previousLinkedAccount.getBakongId().equals(currentUser.getBakongId())) {
        previousLinkedAccount.setLinkedStatus(LinkedStatusEnum.UNLINKED);
        accountRepository.save(previousLinkedAccount);
      } else {
        throw new BizException(ResponseMessage.ACCOUNT_ALREADY_LINKED);
      }
    }

    // Get CDRB account detail & update account table
    return of(cdrbRestClient::getAccountDetail)
        .andThen(peek(AccountValidator::validateAccountAndKYCStatus))
        .andThen(cdrbAccount -> accountMapper.toAccountEntity(pendingAccount, cdrbAccount))
        .andThen(peek(accountRepository::save))
        .andThen(account -> accountMapper.toFinishLinkAccountResponse())
        .apply(accountDetailRequest);
  }

  private void validateFinishLinkAccountRequest(FinishLinkAccountRequest request) {
    if (StringUtils.isEmpty(request.getAccNumber())) {
      throw new BizException(ResponseMessage.MISSING_ACC_NUMBER);
    }

    if (!hasVerfiedOTP()) {
      throw new BizException(ResponseMessage.OTP_NOT_VERIFIED);
    }
  }

  private boolean hasVerfiedOTP() {
    if (!properties.isInitLinkRequiredOtp()) {
      return true;
    }

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    AccountEntity byUserIdAndBakongIdAndLinkedStatus =
        accountRepository
            .findByUserIdAndBakongIdAndLinkedStatus(
                currentUser.getUserId(), currentUser.getBakongId(), LinkedStatusEnum.PENDING)
            .orElseThrow(() -> new BizException(ResponseMessage.INVALID_TOKEN));

    boolean isOtpYetVerified = byUserIdAndBakongIdAndLinkedStatus.getOtpVerified();
    boolean isOtpVerifiedNotStale =
        byUserIdAndBakongIdAndLinkedStatus
            .getOtpVerifiedDateTime()
            .plus(properties.getPinTimeToLiveInMins() + 1L, ChronoUnit.MINUTES)
            .isAfter(Instant.now());

    return isOtpYetVerified && isOtpVerifiedNotStale;
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
        .andThen(peek(AccountValidator::validateAccountStatus))
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

  @Override
  public boolean checkAccountLinkedWithBakongId(String bakongId, String accountId) {
    return accountRepository.existsByBakongIdAndAccountIdAndLinkedStatus(
        bakongId, accountId, LinkedStatusEnum.COMPLETED);
  }

  private void validateUnlinkAccountRequest(UnlinkAccountRequest unlinkAccountRequest) {
    if (StringUtils.isBlank(unlinkAccountRequest.getAccNumber())) {
      throw new BizException(ResponseMessage.MISSING_ACC_NUMBER);
    }
  }
}
