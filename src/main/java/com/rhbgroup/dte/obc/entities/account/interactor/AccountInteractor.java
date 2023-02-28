package com.rhbgroup.dte.obc.entities.account.interactor;

import com.rhbgroup.dte.obc.common.enums.ResponseMessage;
import com.rhbgroup.dte.obc.entities.Account;
import com.rhbgroup.dte.obc.entities.account.controller.request.AccountRequest;
import com.rhbgroup.dte.obc.entities.account.interactor.gateway.AccountService;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountInteractor {

    private final AccountService accountService;
    private final AuthenticationManager authManager;

    public Account getAccount() {
        return accountService.doSomething();
    }

    public Account initAccount() {
        return accountService.doSomething();
    }

    public Authentication authenticate(AccountRequest accountRequest) {
        try {
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(
                    accountRequest.getUsername(),
                    accountRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;

        } catch (AuthenticationException ex) {
            throw new UserAuthenticationException(ResponseMessage.INVALID_CREDENTIAL);
        }

    }
}
