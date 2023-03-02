package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;

public interface AccountService {

  InitAccountResponse authenticate(InitAccountRequest request);

}
