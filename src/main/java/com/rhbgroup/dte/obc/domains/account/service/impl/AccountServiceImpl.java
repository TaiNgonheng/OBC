package com.rhbgroup.dte.obc.domains.account.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.ServiceType;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
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
  private final ConfigRepository configRepository;
  private final UserAuthService userAuthService;

  private final AccountMapper accountMapper;

  private final PGRestClient pgRestClient;

  private static final String KTC_STATUS = "FULL_KYC";
  private static final String PG1_CACHE_NAME = "PG1_CACHE";

  private static final String PG1_LOGIN_KEY = "Pg1_Login_";

  @PostConstruct
  public void postConstruct() {
    cacheUtil.createCache(PG1_CACHE_NAME, Duration.ONE_MINUTE);
  }

  @Override
  public InitAccountResponse authenticate(InitAccountRequest request) {

    //    return Functions.of(accountMapper::toModel)
    //            .andThen(userAuthService::authenticate)
    //            .andThen(accountMapper::toResponseWrapper)
    //            .apply(request);

    return null;
  }

  @Override
  public InitAccountResponse initLinkAccount(InitAccountRequest request) {
    InitAccountResponse accountResponse = new InitAccountResponse();
    UserModel userModel = new UserModel();
    userModel.setUsername(request.getLogin());
    userModel.setPassword(request.getKey());

    Authentication authentication = userAuthService.authenticate(userModel);
    String jwtKey = PG1_LOGIN_KEY.concat(request.getLogin());
    // validate pg1 jwt token
    String jwt = cacheUtil.getValueFromKey(PG1_CACHE_NAME, jwtKey);
    ConfigEntity configEntity =
        configRepository
            .getByServiceName(ServiceType.PG1.getName())
            .orElseThrow(() -> new BizException(ResponseMessage.NO_CONFIG_FOR_SERVICE_FOUND));
    if (jwt == null) {
      PGAuthRequest pgAuthRequest =
          new PGAuthRequest().username(configEntity.getLogin()).password(configEntity.getSecret());

      PGAuthResponse pgAuthResponse = pgRestClient.login(pgAuthRequest);
      if (pgAuthResponse == null) {
        throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
      }
      jwt = pgAuthResponse.getIdToken();
      cacheUtil.addKey(PG1_CACHE_NAME, jwtKey, jwt);
    }
    // validate pg1 profile
    Map<String, String> param = new HashMap<>();
    param.put("account_id", userModel.getUsername());
    PGProfileResponse userProfile = pgRestClient.getUserProfile(param, jwt);
    if (userProfile == null) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }

    if (!KTC_STATUS.equals(userProfile.getKycStatus())) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
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
    data.setRequireOtp(configEntity.isRequiredTrxOtp() ? 1 : 0);
    // generate infoBip OTP
    // generate JWT token
    data.setAccessToken(jwtTokenUtils.generateJwt(authentication));
    accountResponse.setStatus(new ResponseStatus().code(0));
    accountResponse.setData(data);
    return accountResponse;
  }
}
