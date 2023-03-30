package com.rhbgroup.dte.obc.domains.account.controller;

import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
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
  public ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest verifyOtpRequest) {
    return Functions.of(accountService::verifyOtp)
        .andThen(ResponseEntity::ok)
        .apply(verifyOtpRequest);
  }

  @Override
  public ResponseEntity<FinishLinkAccountResponse> finishLinkAccount(
      String authorization, FinishLinkAccountRequest request) {
    return Functions.of(accountService::finishLinkAccount)
        .andThen(ResponseEntity::ok)
        .apply(authorization, request);
  }

  @Override
  public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
    return Functions.of(accountService::authenticate).andThen(ResponseEntity::ok).apply(request);
  }

  @Override
  public ResponseEntity<GetAccountDetailResponse> getAccountDetail(
      GetAccountDetailRequest getAccountDetailRequest) {
    return Functions.of(accountService::getAccountDetail)
        .andThen(ResponseEntity::ok)
        .apply(getAccountDetailRequest);
  }
}
