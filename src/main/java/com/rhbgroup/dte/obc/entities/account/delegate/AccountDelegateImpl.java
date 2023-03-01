package com.rhbgroup.dte.obc.entities.account.delegate;

import com.rhbgroup.dte.obc.api.DefaultApiDelegate;
import com.rhbgroup.dte.obc.common.utils.func.Functions;
import com.rhbgroup.dte.obc.entities.account.interactor.AccountInteractor;
import com.rhbgroup.dte.obc.entities.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.model.AccountRequest;
import com.rhbgroup.dte.obc.model.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountDelegateImpl implements DefaultApiDelegate {

  private final AccountInteractor accountInteractor;
  private final AccountMapper accountMapper;

  @Override
  public ResponseEntity<AccountResponse> initLinkAccount(AccountRequest accountRequest) {

    return Functions.of(accountInteractor::authenticate)
        .andThen(accountMapper::toAccountResponse)
        .andThen(ResponseEntity::ok)
        .apply(accountRequest);
  }
}
