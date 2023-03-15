package com.rhbgroup.dte.obc.domains.user.controller;

import com.rhbgroup.dte.obc.api.UserExchangeApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.user.mapper.UserExchangeMapper;
import com.rhbgroup.dte.obc.domains.user.service.UserExchangeService;
import com.rhbgroup.dte.obc.model.GWAuthenticationRequest;
import com.rhbgroup.dte.obc.model.GWAuthenticationResponse;
import com.rhbgroup.dte.obc.model.UserExchangeRequest;
import com.rhbgroup.dte.obc.model.UserExchangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserExchangeController implements UserExchangeApiDelegate {

  private final UserExchangeMapper userExchangeMapper;

  private final UserExchangeService userExchangeService;

  @Override
  public ResponseEntity<UserExchangeResponse> userExchange(
      UserExchangeRequest userExchangeRequest) {

    return Functions.of(userExchangeMapper::toModel)
        .andThen(userExchangeService::exchangeUser)
        .andThen(userExchangeMapper::toResponse)
        .andThen(ResponseEntity::ok)
        .apply(userExchangeRequest);
  }

  @Override
  public ResponseEntity<GWAuthenticationResponse> revokeToken(
      GWAuthenticationRequest gwAuthenticationRequest) {

    return Functions.of(userExchangeMapper::fromAuthRequestToModel)
        .andThen(userExchangeService::revokeToken)
        .andThen(userExchangeMapper::toAuthResponse)
        .andThen(ResponseEntity::ok)
        .apply(gwAuthenticationRequest);
  }
}
