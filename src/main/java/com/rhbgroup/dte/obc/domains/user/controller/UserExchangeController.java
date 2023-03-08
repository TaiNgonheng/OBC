package com.rhbgroup.dte.obc.domains.user.controller;

import com.rhbgroup.dte.obc.api.UserExchangeApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.user.mapper.UserExchangeMapper;
import com.rhbgroup.dte.obc.domains.user.service.UserExchangeService;
import com.rhbgroup.dte.obc.model.UserExchangeRequest;
import com.rhbgroup.dte.obc.model.UserExchangeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserExchangeController implements UserExchangeApiDelegate {

  @Autowired private UserExchangeMapper userExchangeMapper;

  @Autowired private UserExchangeService userExchangeService;

  @Override
  public ResponseEntity<UserExchangeResponse> userExchange(
      UserExchangeRequest userExchangeRequest) {

    return Functions.of(userExchangeMapper::toModel)
        .andThen(userExchangeService::exchangeUser)
        .andThen(userExchangeMapper::toResponse)
        .andThen(ResponseEntity::ok)
        .apply(userExchangeRequest);
  }
}
