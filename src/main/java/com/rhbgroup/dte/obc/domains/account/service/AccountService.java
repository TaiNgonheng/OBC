package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;

public interface AccountService {

  AuthenticationResponse authenticate(AuthenticationRequest request);

  InitAccountResponse initLinkAccount(InitAccountRequest request);

  VerifyOtpResponse verifyOtp(String authorization, VerifyOtpRequest request);

  VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

  FinishLinkAccountResponse finishLinkAccount(
      String authorization, FinishLinkAccountRequest request);

  GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request);
}
