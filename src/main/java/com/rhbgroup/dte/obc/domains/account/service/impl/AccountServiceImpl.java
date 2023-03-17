package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.ObcStringUtils;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGAuthRequest;
import com.rhbgroup.dte.obc.model.PGAuthResponse;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final AccountMapper accountMapper;
  private final PGRestClient pgRestClient;

  @PostConstruct
  public void postConstruct() {
    cacheUtil.createCache(CacheConstants.PGCache.CACHE_NAME, Duration.ONE_MINUTE);
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

    if (pgToken == null) {

      ConfigService pg1Config =
          this.configService.loadJSONValue(ConfigConstants.PGConfig.PG1_ACCOUNT_KEY);
      String username =
          pg1Config.getValue(ConfigConstants.PGConfig.PG1_DATA_USERNAME_KEY, String.class);
      String password =
          pg1Config.getValue(ConfigConstants.PGConfig.PG1_DATA_PASSWORD_KEY, String.class);

      PGAuthRequest pgAuthRequest = new PGAuthRequest().username(username).password(password);
      PGAuthResponse pgAuthResponse = pgRestClient.login(pgAuthRequest);

      cacheUtil.addKey(CacheConstants.PGCache.CACHE_NAME, pgLoginKey, pgAuthResponse.getIdToken());
      pgToken = pgAuthResponse.getIdToken();
    }

    // Get PG user profile
    Map<String, String> param = new HashMap<>();
    param.put("account_id", userModel.getUsername());
    PGProfileResponse userProfile = pgRestClient.getUserProfile(param, pgToken);

    if (!KycStatusEnum.parse(userProfile.getKycStatus()).equals(KycStatusEnum.FULL_KYC)) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }

    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
    if (!userProfile.getPhone().equals(request.getPhoneNumber())) {
      data.setRequireChangePhone(1);
      data.setLast3DigitsPhone(ObcStringUtils.getLast3DigitsPhone(userProfile.getPhone()));
    }
    // get require OTP config
    Integer otpEnabled =
        configService.getByConfigKey(
            ConfigConstants.REQUIRED_INIT_ACCOUNT_OTP_KEY, ConfigConstants.VALUE, Integer.class);
    data.setRequireOtp(otpEnabled);

    // TODO generate infoBip OTP

    // generate JWT token
    String bakingLoginJwt = jwtTokenUtils.generateJwt(authentication);
    cacheUtil.addKey(
        CacheConstants.PGCache.CACHE_NAME,
        CacheConstants.PGCache.PG1_LOGIN_KEY.concat(request.getLogin()),
        bakingLoginJwt);
    data.setAccessToken(bakingLoginJwt);

    return new InitAccountResponse().status(new ResponseStatus().code(0)).data(data);
  }
}
