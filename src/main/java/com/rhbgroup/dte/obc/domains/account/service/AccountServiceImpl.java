package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.ServiceType;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.DateTimeUtil;
import com.rhbgroup.dte.obc.common.util.SpringRestUlti;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.dto.request.BakongLoginRequest;
import com.rhbgroup.dte.obc.dto.response.BakongGetProfileResponse;
import com.rhbgroup.dte.obc.dto.response.BakongLoginResponse;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
  @Resource
  private JwtTokenUtils jwtTokenUtils;
  @Resource
  private AuthenticationManager authManager;
  @Resource
  private SpringRestUlti springRestUlti;
  @Resource
  private CacheUtil cacheUtil;
  @Resource
  private DateTimeUtil dateTimeUtil;
  @Resource
  private UserProfileRepository userProfileRepository;
  @Resource
  private ConfigRepository configRepository;
  private final String KTC_STATUS = "FULL_KYC";

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
    Optional<UserProfileEntity> userProfile =
        userProfileRepository.getByUsernameAndPassword(request.getLogin(), request.getKey());
    if (userProfile.isEmpty()) {
      accountResponse.setStatus(new ResponseStatus().errorCode("4"));
      return accountResponse;
    }
    String jwtKey = request.getLogin().concat("_pg1_").concat(dateTimeUtil.dateToKey(new Date()));
    //validate pg1 jwt token
    String jwt = cacheUtil.getValueFromKey(jwtKey);
    if (jwt == null || jwtTokenUtils.isExpired(jwt)) {
      Optional<ConfigEntity> configEntity =
          configRepository.getByServiceName(ServiceType.PG1.getName());
      BakongLoginRequest bakongLoginRequest =
          new BakongLoginRequest(request.getLogin(), request.getKey());
      BakongLoginResponse bakongLoginResponse =
          springRestUlti.sendPost(
              "",
              bakongLoginRequest,
              ParameterizedTypeReference.forType(BakongLoginResponse.class));
      if (bakongLoginResponse == null) {
        accountResponse.setStatus(new ResponseStatus().errorCode("1"));
        return accountResponse;
      }
      cacheUtil.addKey(jwtKey, bakongLoginResponse.getId_token());
    }
    // validate pg1 profile
    Map<String, String> header = new HashMap<>();
    header.put("Authorization", "Bearer ".concat(jwt));
    BakongGetProfileResponse bakongUserProfile =
        springRestUlti.sendGet(
            "", header, ParameterizedTypeReference.forType(BakongGetProfileResponse.class));
    if (bakongUserProfile == null) {
      accountResponse.setStatus(new ResponseStatus().errorCode("1"));
      return accountResponse;
    }
    if (!KTC_STATUS.equals(bakongUserProfile.getKycStatus())) {
      accountResponse.setStatus(new ResponseStatus().errorCode("1"));
      return accountResponse;
    }
    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
    if (bakongUserProfile.getPhone().equals(request.getPhoneNumber())) {
      data.setRequireChangePhone(1);
      data.setLast3DigitsPhone(bakongUserProfile.getPhone());
    }
    // get require OTP config
    Optional<ConfigEntity> configEntity =
        configRepository.getByServiceName(ServiceType.INFO_BIP.getName());
    data.setRequireOtp(configEntity.isPresent() && configEntity.get().isRequiredTrxOtp() ? 1 : 0);
    // generate infoBip OTP
    // generate JWT token
    accountResponse.setStatus(new ResponseStatus().code(0));
    accountResponse.setData(data);
    return accountResponse;
  }
}
