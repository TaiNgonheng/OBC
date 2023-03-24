package com.rhbgroup.dte.obc.acount;

import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
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
    return new VerifyOtpRequest().otpCode("000000");
  }

  protected PGProfileResponse mockProfileRequiredChangeMobile() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("95500000000")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileNotFullyKyc() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.PARTIAL_KYC.getName())
        .phone("95500000000")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileUserDeactivated() {
    return new PGProfileResponse()
        .accountId("BankAccountId")
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("95500000000")
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

  protected InfoBipVerifyOtpResponse mockInfoBipVerifyOtpResponse() {
    return new InfoBipVerifyOtpResponse()
            .pinId("pinId")
            .msisdn("msisdn")
            .attemptsRemaining(1)
            .verified(true);
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
