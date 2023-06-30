package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.AccountFilterCondition;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.UnlinkAccountRequest;
import com.rhbgroup.dte.obc.model.UnlinkAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;

public interface AccountService {

  AuthenticationResponse authenticate(AuthenticationRequest request);

  InitAccountResponse initLinkAccount(InitAccountRequest request);

  VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

  FinishLinkAccountResponse finishLinkAccount(FinishLinkAccountRequest request);

  GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request);

  AccountModel getActiveAccount(AccountFilterCondition condition);

  UnlinkAccountResponse unlinkAccount(UnlinkAccountRequest unlinkAccountRequest);

  boolean checkAccountLinkedWithBakongId(String bakongId, String accountId);
}
