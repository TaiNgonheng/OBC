package com.rhbgroup.dte.obc.acount;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.AuthenticationResponseAllOfData;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponseAcct;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.InfoBipSendOtpResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.LoginTypeEnum;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.codehaus.plexus.util.Base64;
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
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("85500000000")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileNotFullyKyc() {
    return new PGProfileResponse()
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.PARTIAL_KYC.getName())
        .phone("85500000000")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileUserDeactivated() {
    return new PGProfileResponse()
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("85500000000")
        .accountStatus(AccountStatusEnum.DEACTIVATED.getStatus());
  }

  protected PGProfileResponse mockProfileNotRequiredChangeMobile() {
    return new PGProfileResponse()
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone(MOBILE_NUMBER)
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected FinishLinkAccountRequest mockFinishLinkAccountRequest() {
    return new FinishLinkAccountRequest().accNumber("10000xxx");
  }

  protected FinishLinkAccountResponse mockFinishLinkAccountResponse() {
    return new FinishLinkAccountResponse()
        .status(ResponseHandler.ok())
        .data(new FinishLinkAccountResponseAllOfData().requireChangePassword(false));
  }

  protected InfoBipSendOtpResponse mockInfoBipSendOtpResponse() {
    return new InfoBipSendOtpResponse().pinId("pinId");
  }

  protected Authentication mockAuthentication() {
    return new UsernamePasswordAuthenticationToken("test", "test");
  }

  protected String mockJwtToken() {
    return "header.payload.signature";
  }

  protected String mockBearerString() {
    return "Bearer "
        .concat(
            new String(
                Base64.encodeBase64("bearerToken".getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8));
  }

  protected UserModel mockUserModel() {
    return new UserModel().cifNo("123xxx").id(BigDecimal.ONE);
  }

  protected CDRBGetAccountDetailResponse mockCdrbAccountResponse() {
    return new CDRBGetAccountDetailResponse()
        .acct(
            new CDRBGetAccountDetailResponseAcct()
                .accountNo("123xxx")
                .kycStatus(CDRBGetAccountDetailResponseAcct.KycStatusEnum.F));
  }

  protected CDRBGetAccountDetailResponse mockCdrbAccountResponseNotKYC() {
    return new CDRBGetAccountDetailResponse()
        .acct(
            new CDRBGetAccountDetailResponseAcct()
                .accountNo("123xxx")
                .kycStatus(CDRBGetAccountDetailResponseAcct.KycStatusEnum.V));
  }

  protected AccountEntity mockAccountEntity() {
    AccountEntity accountEntity = new AccountEntity();
    accountEntity.setId(1L);
    accountEntity.setUserId(1L);
    accountEntity.setAccountId(mockCdrbAccountResponse().getAcct().getAccountNo());

    return accountEntity;
  }
}
