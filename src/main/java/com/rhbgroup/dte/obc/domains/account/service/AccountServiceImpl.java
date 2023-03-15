package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.ServiceType;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.DateTimeUtil;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.dto.request.BakongLoginRequest;
import com.rhbgroup.dte.obc.dto.response.BakongGetProfileResponse;
import com.rhbgroup.dte.obc.dto.response.BakongLoginResponse;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.cache.expiry.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
  @Resource private JwtTokenUtils jwtTokenUtils;
  @Resource private AuthenticationManager authManager;
  @Resource private SpringRestUtil springRestUlti;
  @Resource private CacheUtil cacheUtil;
  @Resource private DateTimeUtil dateTimeUtil;
  @Resource private UserProfileRepository userProfileRepository;
  @Resource private ConfigRepository configRepository;
  @Resource private UserAuthService userAuthService;
  @Resource private PGRestClient pgRestClient;
  private final String KTC_STATUS = "FULL_KYC";

  private final String PG1_JWT_CACHE_NAME = "pg1JwtCache";

  @PostConstruct
  public void postConstruct() {
    cacheUtil.createCache(PG1_JWT_CACHE_NAME, Duration.ONE_MINUTE);
  }

  @Override
  public InitAccountResponse authenticate(InitAccountRequest request) {

    return Functions.of(this::supplySecurityContext)
        .andThen(this::buildResponse)
        .apply(request.getLogin(), request.getKey());
  }

  private Authentication supplySecurityContext(String login, String key) {
    try {
      Authentication authentication =
          authManager.authenticate(new UsernamePasswordAuthenticationToken(login, key));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      return authentication;

    } catch (AuthenticationException ex) {
      throw new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED);
    }
  }

  private InitAccountResponse buildResponse(Authentication authentication) {

    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
    data.setAccessToken(jwtTokenUtils.generateJwt(authentication));
    data.setLast3DigitsPhone("123");
    data.setRequireOtp(1);
    data.setRequireChangePhone(0);

    InitAccountResponse accountResponse = new InitAccountResponse();
    accountResponse.setStatus(new ResponseStatus().code(0));
    accountResponse.setData(data);

    return accountResponse;
  }

  @Override
  public InitAccountResponse initLinkAccount(InitAccountRequest request) {
    InitAccountResponse accountResponse = new InitAccountResponse();
    UserModel userModel = new UserModel();
    userModel.setUsername(request.getLogin());
    userModel.setPassword(request.getKey());
    Authentication authentication = userAuthService.authenticate(userModel);
    String jwtKey = "Pg1_Login_".concat(request.getLogin());
    // validate pg1 jwt token
    String jwt = cacheUtil.getValueFromKey(PG1_JWT_CACHE_NAME, jwtKey);
    if (jwt == null) {
      Optional<ConfigEntity> configEntity =
          configRepository.getByServiceName(ServiceType.PG1.getName());
      BakongLoginRequest bakongLoginRequest =
          new BakongLoginRequest(request.getLogin(), request.getKey());
      BakongLoginResponse bakongLoginResponse = pgRestClient.login(bakongLoginRequest);
      if (bakongLoginResponse == null) {
        throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
      }
      cacheUtil.addKey(PG1_JWT_CACHE_NAME, jwtKey, bakongLoginResponse.getId_token());
    }
    // validate pg1 profile
    Map<String, String> param = new HashMap<>();
    param.put("account_id",userModel.getUsername());
    BakongGetProfileResponse bakongUserProfile = pgRestClient.getUserProfile(param, jwt);
    if (bakongUserProfile == null) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
    if (!KTC_STATUS.equals(bakongUserProfile.getKycStatus())) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
    if (!bakongUserProfile.getPhone().equals(request.getPhoneNumber())) {
      data.setRequireChangePhone(1);
      data.setLast3DigitsPhone(
          bakongUserProfile.getPhone().length() > 3
              ? bakongUserProfile.getPhone().substring(bakongUserProfile.getPhone().length() - 3)
              : bakongUserProfile.getPhone());
    }
    // get require OTP config
    Optional<ConfigEntity> configEntity =
        configRepository.getByServiceName(ServiceType.INFO_BIP.getName());
    data.setRequireOtp(configEntity.isPresent() && configEntity.get().isRequiredTrxOtp() ? 1 : 0);
    // generate infoBip OTP
    // generate JWT token
    data.setAccessToken(jwtTokenUtils.generateJwt(authentication));
    accountResponse.setStatus(new ResponseStatus().code(0));
    accountResponse.setData(data);
    return accountResponse;
  }
}
