package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.ObcStringUtils;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapperImpl;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.InfoBipVerifyOtpResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGAuthRequest;
import com.rhbgroup.dte.obc.model.PGAuthResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import com.rhbgroup.dte.obc.rest.CdrbRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Collections;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final JwtTokenUtils jwtTokenUtils;
  private final CacheUtil cacheUtil;
  private final UserAuthService userAuthService;
  private final ConfigService configService;
  private final PGRestClient pgRestClient;
  private final CdrbRestClient cdrbRestClient;
  private final AccountRepository accountRepository;
  private final AccountMapper accountMapper = new AccountMapperImpl();

  @PostConstruct
  public void postConstruct() {
    cacheUtil.createCache(CacheConstants.PGCache.CACHE_NAME, Duration.ONE_MINUTE);
    cacheUtil.createCache(CacheConstants.CDRBCache.CACHE_NAME, Duration.ONE_MINUTE);
  }

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

    String pgLoginKey = CacheConstants.PGCache.PG1_LOGIN_KEY.concat(request.getLogin());

    // Get PG user profile, trigger OTP and build response
    return Functions.of(cacheUtil::getValueFromKey)
        .andThen(cacheValue -> generateKey(cacheValue, pgLoginKey))
        .andThen(
            jwtToken ->
                pgRestClient.getUserProfile(
                    Collections.singletonList(request.getBakongAccId()), jwtToken))
        .andThen(Functions.peek(this::validateAccount))
        .andThen(Functions.peek(this::triggerOTP))
        .andThen(profileResponse -> buildResponse(request, profileResponse, token))
        .apply(CacheConstants.PGCache.CACHE_NAME, pgLoginKey);
  }

  private void triggerOTP(PGProfileResponse pgProfileResponse) {
    // TODO need to implement
  }

  private InitAccountResponse buildResponse(
      InitAccountRequest request, PGProfileResponse userProfile, String jwtToken) {

    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
    data.setAccessToken(jwtToken);

    if (!userProfile.getPhone().equals(request.getPhoneNumber())) {
      data.setRequireChangePhone(true);
      data.setLast3DigitsPhone(ObcStringUtils.getLast3DigitsPhone(userProfile.getPhone()));
    } else {
      data.setRequireChangePhone(false);
    }
    // get require OTP config
    Integer otpEnabled =
        configService.getByConfigKey(
            ConfigConstants.REQUIRED_INIT_ACCOUNT_OTP_KEY, ConfigConstants.VALUE, Integer.class);
    data.setRequireOtp(otpEnabled == 1);

    return new InitAccountResponse().status(ResponseHandler.ok()).data(data);
  }

  private String generateKey(String pgToken, String pgLoginKey) {
    // Validate pgToken token
    if (StringUtils.isNotBlank(pgToken) && !jwtTokenUtils.isExtTokenExpired(pgToken)) {
      return pgToken;
    }
    ConfigService configServiceInstance =
        configService.loadJSONValue(ConfigConstants.PGConfig.PG1_ACCOUNT_KEY);

    String username =
        configServiceInstance.getValue(
            ConfigConstants.PGConfig.PG1_DATA_USERNAME_KEY, String.class);
    String password =
        configServiceInstance.getValue(
            ConfigConstants.PGConfig.PG1_DATA_PASSWORD_KEY, String.class);

    return Functions.of(pgRestClient::login)
        .andThen(PGAuthResponseAllOfData::getIdToken)
        .andThen(
            Functions.peek(
                idToken ->
                    cacheUtil.addKey(CacheConstants.PGCache.CACHE_NAME, pgLoginKey, idToken)))
        .apply(new PGAuthRequest().username(username).password(password));
  }

  private void validateAccount(PGProfileResponse userProfile) {
    if (!KycStatusEnum.parse(userProfile.getKycStatus()).equals(KycStatusEnum.FULL_KYC)) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }

    if (AccountStatusEnum.parse(userProfile.getAccountStatus())
        .equals(AccountStatusEnum.DEACTIVATED)) {
      throw new BizException(ResponseMessage.ACCOUNT_DEACTIVATED);
    }
  }

  @Override
  public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
    InfoBipVerifyOtpResponse infoBipVerifyOtpResponse = new InfoBipVerifyOtpResponse();
    // TODO check infoBip OTP
    // TODO check OTP expired

    infoBipVerifyOtpResponse.setVerified(false);
    // validate infoBip response
    VerifyOtpResponseAllOfData data =
        new VerifyOtpResponseAllOfData().isValid(infoBipVerifyOtpResponse.getVerified());
    return new VerifyOtpResponse().status(new ResponseStatus().code(0)).data(data);
  }

  @Override
  public FinishLinkAccountResponse finishLinkAccount(
      String authorization, FinishLinkAccountRequest request) {
    String cdrbLoginKey =
        CacheConstants.CDRBCache.CDRB_LOGIN_KEY.concat(
            jwtTokenUtils.getUsernameFromJwtToken(jwtTokenUtils.extractJwt(authorization)));
    // Validate pgToken token
    String cdrbToken = cacheUtil.getValueFromKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey);
    ConfigService cdrbConfig =
        this.configService.loadJSONValue(ConfigConstants.CDRB.CDRB_CREDENTIAL_KEY);

    if (StringUtils.isBlank(cdrbToken) || jwtTokenUtils.isExtTokenExpired(cdrbToken)) {
      // CDRB login
      cdrbToken = crdbAuthenticate(cdrbConfig, cdrbLoginKey);
    }
    Object accountDetail = cdrbRestClient.getAccountDetail(new HashMap<>());
    while (accountDetail==null){
      cdrbToken = crdbAuthenticate(cdrbConfig, cdrbLoginKey);
      accountDetail = cdrbRestClient.getAccountDetail(new HashMap<>());
    }
    // validate account
    accountRepository.save(new AccountEntity());
    return buildResponse();
  }

  private String crdbAuthenticate(ConfigService configService, String cdrbLoginKey) {
    String hmsKey = cdrbRestClient.getHmsKey();
    Object cdrbAuthResponse = cdrbRestClient.login(new Object());
    cacheUtil.addKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey, null);
    return "";
  }
  private FinishLinkAccountResponse buildResponse() {
    FinishLinkAccountResponseAllOfData data = new FinishLinkAccountResponseAllOfData();
    data.setRequireChangePassword(false);
    return new FinishLinkAccountResponse().status(ResponseHandler.ok()).data(data);
  }
}
