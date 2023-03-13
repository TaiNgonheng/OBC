package com.rhbgroup.dte.obc.domains.account.controller;

import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountController implements AccountApiDelegate {

  private final AccountService accountService;

  @Override
  public ResponseEntity<InitAccountResponse> initLinkAccount(InitAccountRequest request) {
    return Functions.of(accountService::authenticate).andThen(ResponseEntity::ok).apply(request);
  }
}
