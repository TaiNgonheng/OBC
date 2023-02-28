package com.rhbgroup.dte.obc.entities.account.controller.response;

import com.rhbgroup.dte.obc.entities.Account;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder(toBuilder = true)
@Getter
public class AccountResponse {
    private Account account;
    private String token;
    private String refreshToken;
    private BigDecimal expiredAt;
}
