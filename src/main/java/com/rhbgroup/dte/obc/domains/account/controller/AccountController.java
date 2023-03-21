package com.rhbgroup.dte.obc.domains.account.controller;

import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
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
    return Functions.of(accountService::initLinkAccount).andThen(ResponseEntity::ok).apply(request);
  }

  @Override
  public ResponseEntity<FinishLinkAccountResponse> finishLinkAccount(
      String authorization, FinishLinkAccountRequest request) {
    return Functions.of(accountService::finishLinkAccount)
        .andThen(ResponseEntity::ok)
        .apply(authorization, request);
  }
}
