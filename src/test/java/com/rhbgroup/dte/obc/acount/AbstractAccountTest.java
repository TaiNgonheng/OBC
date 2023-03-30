package com.rhbgroup.dte.obc.acount;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.AuthenticationResponseAllOfData;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.LoginTypeEnum;
import com.rhbgroup.dte.obc.model.PGAuthResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public abstract class AbstractAccountTest {

  protected static final String MOBILE_NUMBER = "85512345678";

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
    return new VerifyOtpRequest().otpCode("000000");
  }

  protected AuthenticationRequest mockAuthenticationRequest() {
    return new AuthenticationRequest()
        .loginType(LoginTypeEnum.USER_PWD)
        .login("admin")
        .key("mypassword");
  }

  protected AuthenticationResponse mockAuthenticationResponse() {
    return new AuthenticationResponse()
        .status(ResponseHandler.ok())
        .data(
            new AuthenticationResponseAllOfData()
                .accessToken(mockJwtToken())
                .requireChangePassword(false));
  }

  protected PGProfileResponse mockProfileRequiredChangeMobile() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("85500000000")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileNotFullyKyc() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.PARTIAL_KYC.getName())
        .phone("85500000000")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileUserDeactivated() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("85500000000")
        .accountStatus(AccountStatusEnum.DEACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileNotRequiredChangeMobile() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone(MOBILE_NUMBER)
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGAuthResponseAllOfData mockPGAuthResponse() {
    return new PGAuthResponseAllOfData().idToken(mockJwtToken());
  }

  protected Authentication mockAuthentication() {
    return new UsernamePasswordAuthenticationToken("test", "test");
  }

  protected String mockJwtToken() {
    return "header.payload.signature";
  }
}
