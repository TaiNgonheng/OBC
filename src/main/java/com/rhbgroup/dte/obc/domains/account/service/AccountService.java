package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;

public interface AccountService {

  AuthenticationResponse authenticate(AuthenticationRequest request);

  InitAccountResponse initLinkAccount(InitAccountRequest request);
}
