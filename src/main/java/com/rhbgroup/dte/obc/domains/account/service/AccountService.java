package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;

public interface AccountService {

  InitAccountResponse authenticate(InitAccountRequest request);

  InitAccountResponse initLinkAccount(InitAccountRequest request);

  VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

  GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request);
}
