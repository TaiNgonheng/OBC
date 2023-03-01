package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.model.AccountRequest;
import org.springframework.security.core.Authentication;

public interface AccountService {

    Authentication authenticate(AccountRequest accountRequest);
}
