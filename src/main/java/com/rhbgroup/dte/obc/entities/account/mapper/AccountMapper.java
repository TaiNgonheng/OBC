package com.rhbgroup.dte.obc.entities.account.mapper;

import com.rhbgroup.dte.obc.entities.Account;
import com.rhbgroup.dte.obc.entities.account.controller.request.AccountRequest;
import com.rhbgroup.dte.obc.entities.account.controller.response.AccountResponse;
import com.rhbgroup.dte.obc.security.JwtTokenManager;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    public AccountResponse toAccountResponse(AccountRequest accountRequest, Authentication authentication) {
        return AccountResponse.builder()
                .account(Account.builder()
                        .username(accountRequest.getUsername())
                        .password(accountRequest.getPassword())
                        .build())
                .token(jwtTokenUtils.generateJwt(authentication))
                .build();
    }
}
