package com.rhbgroup.dte.obc.acount;

import com.rhbgroup.dte.obc.model.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public abstract class AbstractAccountTest {

  protected static final String MOBILE_NUMBER = "95512345678";

  protected InitAccountRequest mockInitAccountRequest() {
    return new InitAccountRequest()
        .key("user")
        .login("login")
        .phoneNumber(MOBILE_NUMBER)
        .loginType(LoginTypeEnum.USER_PWD)
        .bakongAccId("123xxx");
  }

  protected InitAccountResponse mockInitAccountResponse() {
    return new InitAccountResponse()
        .status(new ResponseStatus().code(0))
        .data(new InitAccountResponseAllOfData().accessToken("access_token"));
  }

  protected VerifyOtpResponse mockVerifyOtpResponse() {
    return new VerifyOtpResponse()
            .status(new ResponseStatus().code(0))
            .data(new VerifyOtpResponseAllOfData().isValid(true));
  }

  protected VerifyOtpRequest mockVerifyOtpRequest() {
    return new VerifyOtpRequest()
            .otpCode("000000");
  }

  protected PGProfileResponse mockProfileRequiredChangeMobile() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus("FULL_KYC")
        .phone("95500000000");
  }

  protected PGProfileResponse mockProfileNotRequiredChangeMobile() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus("FULL_KYC")
        .phone(MOBILE_NUMBER);
  }

  protected Authentication mockAuthentication() {
    return new UsernamePasswordAuthenticationToken("test", "test");
  }

  protected String mockJwtToken() {
    return "header.payload.signature";
  }

  protected AccountModel mockAccountModel() {
    return new AccountModel().user(new UserModel().username("test").password("test"));
  }
}
