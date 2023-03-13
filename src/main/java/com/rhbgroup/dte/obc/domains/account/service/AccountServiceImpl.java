package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.ServiceType;
import com.rhbgroup.dte.obc.common.util.SpringRestUlti;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import com.rhbgroup.dte.obc.dto.TestRequestDto;
import com.rhbgroup.dte.obc.dto.TestResponseDto;
import com.rhbgroup.dte.obc.dto.request.BakongLoginRequest;
import com.rhbgroup.dte.obc.dto.response.BakongLoginResponse;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

  @Autowired private AuthenticationManager authManager;

  @Autowired private JwtTokenUtils jwtTokenUtils;

  @Autowired private SpringRestUlti springRestUlti;

  @Autowired private UserProfileRepository userProfileRepository;

  @Autowired private ConfigRepository configRepository;

  @Override
  public InitAccountResponse authenticate(InitAccountRequest request) {

    Authentication authentication;
    try {
      authentication =
          authManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getLogin(), request.getKey()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (AuthenticationException ex) {
      throw new UserAuthenticationException(ResponseMessage.INVALID_TOKEN);
    }

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
    TestRequestDto testRequestDto = new TestRequestDto();
    testRequestDto.setId("1235");
    testRequestDto.setStatus("available");
    testRequestDto.setName("asd");
    TestResponseDto b = springRestUlti.sendPost("https://petstore.swagger.io/v2/pet",testRequestDto, ParameterizedTypeReference.forType(TestResponseDto.class));
    TestResponseDto a = springRestUlti.sendGet("https://petstore.swagger.io/v2/pet/1",ParameterizedTypeReference.forType(TestResponseDto.class));
    InitAccountResponse accountResponse = new InitAccountResponse();
    Optional<UserProfileEntity> userProfile =
        userProfileRepository.getByUsernameAndPassword(request.getLogin(), request.getKey());
    if (userProfile.isEmpty()) {
      accountResponse.setStatus(new ResponseStatus().errorCode("4"));
      return accountResponse;
    }
    // check pg1 jwt
    String jwt = null;
    if (jwt == null || jwtTokenUtils.isExpired(jwt)) {
      // get pg1 credential
      Optional<ConfigEntity> configEntity =
          configRepository.getByServiceName(ServiceType.PG1.getName());
      // call pg1 api
      BakongLoginRequest bakongLoginRequest = new BakongLoginRequest(request.getLogin(),request.getKey());
      BakongLoginResponse bakongLoginResponse = springRestUlti.sendPost("", bakongLoginRequest, ParameterizedTypeReference.forType(BakongLoginResponse.class));
      if(bakongLoginResponse!=null){

      }
      bakongLoginResponse = new BakongLoginResponse();
      bakongLoginResponse.setId_token("eyJhbGciUxMiJ9.eyJzdWIiOiJtb25pdG9yMSIsImF1dGgiOiJST0xFX01BTkFHRVIiLCJwZ\n" +
              "XJtaXNzaW9ucyI6W10sImlkIjo0NDAyLCJleHAiOjE2NDMyNjc5NDl9.fBdsbaL4NDRnDj\n" +
              "H_CG3JVaaQAZSaEUdjowd8XhJ_JcrJwyJqNsLpAs4A65TgbgTw-P6gJj8qES-E9CffcWq\n" +
              "K-g");
      jwt = bakongLoginResponse.getId_token();
      // cache jwt
    }
    // get bakong profile

    // validate profile kyc
    InitAccountResponseAllOfData data = new InitAccountResponseAllOfData();
    // validate phone number
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
