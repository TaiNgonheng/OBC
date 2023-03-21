package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
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
import com.rhbgroup.dte.obc.rest.CdrbRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
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
    AccountModel accountModel = accountMapper.toModel(request);
    UserModel userModel = accountModel.getUser();

    Authentication authentication = userAuthService.authenticate(userModel);
    String pgLoginKey = CacheConstants.PGCache.PG1_LOGIN_KEY.concat(request.getLogin());

    // Validate pgToken token
    String pgToken = cacheUtil.getValueFromKey(CacheConstants.PGCache.CACHE_NAME, pgLoginKey);

    if (StringUtils.isBlank(pgToken) || jwtTokenUtils.isExtTokenExpired(pgToken)) {

      ConfigService pg1Config =
          this.configService.loadJSONValue(ConfigConstants.PGConfig.PG1_ACCOUNT_KEY);
      String username =
          pg1Config.getValue(ConfigConstants.PGConfig.PG1_DATA_USERNAME_KEY, String.class);
      String password =
          pg1Config.getValue(ConfigConstants.PGConfig.PG1_DATA_PASSWORD_KEY, String.class);

      PGAuthRequest pgAuthRequest = new PGAuthRequest().username(username).password(password);
//      PGAuthResponse pgAuthResponse = pgRestClient.login(pgAuthRequest);
//
//      cacheUtil.addKey(CacheConstants.PGCache.CACHE_NAME, pgLoginKey, pgAuthResponse.getIdToken());
//      pgToken = pgAuthResponse.getIdToken();
    }

    // Get PG user profile
    Map<String, String> param = new HashMap<>();
    param.put("account_id", userModel.getUsername());
//    PGProfileResponse userProfile = pgRestClient.getUserProfile(param, pgToken);
//
//    if (!KycStatusEnum.parse(userProfile.getKycStatus()).equals(KycStatusEnum.FULL_KYC)) {
//      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
//    }

    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
//    if (!userProfile.getPhone().equals(request.getPhoneNumber())) {
//      data.setRequireChangePhone(1);
//      data.setLast3DigitsPhone(ObcStringUtils.getLast3DigitsPhone(userProfile.getPhone()));
//    }
    // get require OTP config
    Integer otpEnabled =
        configService.getByConfigKey(
            ConfigConstants.REQUIRED_INIT_ACCOUNT_OTP_KEY, ConfigConstants.VALUE, Integer.class);
    data.setRequireOtp(otpEnabled);

    // TODO generate infoBip OTP

    // generate JWT token
    data.setAccessToken(jwtTokenUtils.generateJwt(authentication));
    return new InitAccountResponse().status(new ResponseStatus().code(0)).data(data);
  }

  @Override
  public FinishLinkAccountResponse finishLinkAccount(String authorization, FinishLinkAccountRequest request) {
    String cdrbLoginKey = CacheConstants.CDRBCache.CDRB_LOGIN_KEY.concat(jwtTokenUtils.getUsernameFromJwtToken(jwtTokenUtils.extractJwt(authorization)));
    // Validate pgToken token
    String cdrbToken = cacheUtil.getValueFromKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey);

    if (StringUtils.isBlank(cdrbToken) || jwtTokenUtils.isExtTokenExpired(cdrbToken)) {
      //CDRB login
      ConfigService cdrbConfig = this.configService.loadJSONValue(ConfigConstants.CDRB.CDRB_CREDENTIAL_KEY);
      Object cdrbGetHmsKeyResponse = cdrbRestClient.getHmsKey(new HashMap<>());
      Object cdrbAuthResponse = cdrbRestClient.login(new Object());
      cacheUtil.addKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey, null);
    }
    Object accountDetail = cdrbRestClient.getAccountDetail(new HashMap<>());
    //validate account
    if(accountDetail != null){
      accountRepository.save(new AccountEntity());
    }else{
      //CDRB login
      ConfigService cdrbConfig = this.configService.loadJSONValue(ConfigConstants.CDRB.CDRB_CREDENTIAL_KEY);
      Object cdrbGetHmsKeyResponse = cdrbRestClient.getHmsKey(new HashMap<>());
      Object cdrbAuthResponse = cdrbRestClient.login(new Object());
      cacheUtil.addKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey, null);
    }
    return null;
  }
}
