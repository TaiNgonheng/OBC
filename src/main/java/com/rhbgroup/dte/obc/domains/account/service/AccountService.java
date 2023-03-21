package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;

public interface AccountService {

  InitAccountResponse authenticate(InitAccountRequest request);

  InitAccountResponse initLinkAccount(InitAccountRequest request);

  FinishLinkAccountResponse finishLinkAccount(String authorization, FinishLinkAccountRequest request);
}
