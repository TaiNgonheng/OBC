package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.*;

public interface AccountService {

  AuthenticationResponse authenticate(AuthenticationRequest request);

  InitAccountResponse initLinkAccount(InitAccountRequest request);

  VerifyOtpResponse verifyOtp(String authorization, VerifyOtpRequest request);

  UnlinkAccountResponse unlinkAccount(
      String authorization, UnlinkAccountRequest unlinkAccountRequest);
}
