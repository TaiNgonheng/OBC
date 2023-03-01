package com.rhbgroup.dte.obc.domains.account.mapper;

import com.rhbgroup.dte.obc.model.AccountResponse;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    // TODO using mapstruct
    public AccountResponse toAccountResponse(Authentication authentication) {

        // Test mock response
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccessToken(jwtTokenUtils.generateJwt(authentication));
        accountResponse.setLast3DigitsPhone("123");
        accountResponse.setRequireOtp(1);
        accountResponse.setRequireChangePhone(0);

        return accountResponse;
    }
}
