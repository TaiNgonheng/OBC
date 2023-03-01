package com.rhbgroup.dte.obc.domains.account.delegate;

import com.rhbgroup.dte.obc.api.DefaultApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.model.AccountRequest;
import com.rhbgroup.dte.obc.model.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountDelegateImpl implements DefaultApiDelegate {

  private final AccountService accountService;
  private final AccountMapper accountMapper;

  @Override
  public ResponseEntity<AccountResponse> initLinkAccount(AccountRequest accountRequest) {

    return Functions.of(accountService::authenticate)
        .andThen(accountMapper::toAccountResponse)
        .andThen(ResponseEntity::ok)
        .apply(accountRequest);
  }
}
