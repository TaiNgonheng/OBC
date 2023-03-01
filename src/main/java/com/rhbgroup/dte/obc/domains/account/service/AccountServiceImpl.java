package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.AccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AuthenticationManager authManager;

    @Override
    public Authentication authenticate(AccountRequest accountRequest) {
        try {
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(accountRequest.getLogin(), accountRequest.getKey()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;

        } catch (AuthenticationException ex) {
            throw new UserAuthenticationException(ResponseMessage.INVALID_CREDENTIAL);
        }

    }
}
