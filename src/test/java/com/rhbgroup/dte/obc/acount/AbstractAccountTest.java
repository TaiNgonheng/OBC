package com.rhbgroup.dte.obc.acount;

import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.LoginTypeEnum;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public abstract class AbstractAccountTest {

  protected InitAccountRequest mockInitAccountRequest() {
    return new InitAccountRequest()
        .key("user")
        .login("login")
        .phoneNumber("95512345678")
        .loginType(LoginTypeEnum.USER_PWD)
        .bakongAccId("123xxx");
  }

  protected InitAccountResponse mockInitAccountResponse() {
    return new InitAccountResponse()
        .status(new ResponseStatus().code(0))
        .data(new InitAccountResponseAllOfData().accessToken("access_token"));
  }

  protected PGProfileResponse mockProfileResponse() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus("FULL_KYC")
        .phone("95500000000");
  }

  protected Authentication mockAuthentication() {
    return new UsernamePasswordAuthenticationToken("test", "test");
  }

  protected String mockJwtToken() {
    return "header.payload.signature";
  }
}
