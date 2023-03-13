package com.rhbgroup.dte.obc.domains.account.controller;

import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountController implements AccountApiDelegate {

  @Autowired private AccountService accountService;

  @Override
  public ResponseEntity<InitAccountResponse> initLinkAccount(InitAccountRequest request) {
    return Functions.of(accountService::initLinkAccount).andThen(ResponseEntity::ok).apply(request);
  }
}
