package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.constants.services.CommonServiceConstants;
import com.rhbgroup.dte.obc.common.constants.services.Pg1Constants;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
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
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final JwtTokenUtils jwtTokenUtils;
  private final CacheUtil cacheUtil;
  private final ConfigRepository configRepository;
  private final UserAuthService userAuthService;
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
      ConfigEntity pg1Config =
          configRepository
              .getByConfigKey(Pg1Constants.PG1_ACOUNT_KEY)
              .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
      String username;
      String password;
      try {
        JSONObject pg1AccountData = new JSONObject(pg1Config.getConfigValue());
        username = (String)pg1AccountData.get(Pg1Constants.PG1_DATA_USERNAME_KEY);
        password = (String)pg1AccountData.get(Pg1Constants.PG1_DATA_PASSWORD_KEY);
      }catch (Exception e){
        throw new BizException(ResponseMessage.CONFIGURATION_DATA_INVALID);
      }

      PGAuthRequest pgAuthRequest =
          new PGAuthRequest().username(username).password(password);
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
      data.setLast3DigitsPhone(
          userProfile.getPhone().length() > 3
              ? userProfile.getPhone().substring(userProfile.getPhone().length() - 3)
              : userProfile.getPhone());
    }
    // get require OTP config
    ConfigEntity requireOtpConfig =
        configRepository
            .getByConfigKey(CommonServiceConstants.REQUIRED_INIT_ACCOUNT_OTP_KEY)
            .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
    Integer requireOtpValue;
    try {
      JSONObject requireOtpValueData = new JSONObject(requireOtpConfig.getConfigValue());
      requireOtpValue = (Integer) requireOtpValueData.get(CommonServiceConstants.REQUIRED_INIT_ACCOUNT_OTP_VALUE_KEY);
    }catch (Exception e){
      throw new BizException(ResponseMessage.CONFIGURATION_DATA_INVALID);
    }
    data.setRequireOtp(requireOtpValue);

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
