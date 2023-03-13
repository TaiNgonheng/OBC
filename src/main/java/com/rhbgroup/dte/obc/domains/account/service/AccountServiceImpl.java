package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final JwtTokenUtils jwtTokenUtils;
  private final AuthenticationManager authManager;

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
}
