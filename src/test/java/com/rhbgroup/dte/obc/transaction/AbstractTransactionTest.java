package com.rhbgroup.dte.obc.transaction;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.common.util.RandomGenerator;
import com.rhbgroup.dte.obc.domains.config.service.impl.ConfigServiceImpl;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponseAcct;
import com.rhbgroup.dte.obc.model.CasaAccountStatus;
import com.rhbgroup.dte.obc.model.CasaAccountType;
import com.rhbgroup.dte.obc.model.CasaKYCStatus;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.TransactionType;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.nio.charset.StandardCharsets;
import org.codehaus.plexus.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractTransactionTest {

  private static final String CASA_ACCOUNT_TEST = "123xxx";
  private static final String BAKONG_ACCOUNT_TEST = "bakong@oski";
  private static final String CURRENCY_USD = "USD";
  private static final String PHONE_NUMBER = "85500000000";

  protected InitTransactionRequest mockInitTransactionRequest() {
    return new InitTransactionRequest()
        .type(TransactionType.WALLET)
        .sourceAcc(CASA_ACCOUNT_TEST)
        .destinationAcc(BAKONG_ACCOUNT_TEST)
        .amount(1.0)
        .ccy(CURRENCY_USD)
        .desc("Desc");
  }

  protected InitTransactionResponse mockInitTransactionResponse() {
    return new InitTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new InitTransactionResponseAllOfData()
                .fee(0.0)
                .initRefNumber(RandomGenerator.getDefaultRandom().nextString())
                .requireOtp(false)
                .debitCcy(CURRENCY_USD)
                .debitAmount(12.0));
  }

  protected String mockBearerString() {
    return "Bearer "
        .concat(
            new String(
                Base64.encodeBase64("bearerToken".getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8));
  }

  protected CustomUserDetails mockCustomUserDetails() {
    return CustomUserDetails.builder()
        .userId(1L)
        .username("name")
        .bakongId(BAKONG_ACCOUNT_TEST)
        .password("password")
        .phoneNumber(PHONE_NUMBER)
        .cif("123xxx")
        .enabled(true)
        .build();
  }

  protected AccountModel mockAccountModel() {
    return new AccountModel()
        .accountNo(CASA_ACCOUNT_TEST)
        .accountName("name")
        .accountType(CasaAccountType.D)
        .accountStatus(CasaAccountStatus._1)
        .bakongId(BAKONG_ACCOUNT_TEST)
        .accountCcy(CURRENCY_USD);
  }

  protected AccountModel mockAccountModelSourceAccNotMatched() {
    return new AccountModel()
        .accountNo("different_casa_number")
        .accountName("name")
        .accountType(CasaAccountType.D)
        .accountStatus(CasaAccountStatus._1)
        .bakongId(BAKONG_ACCOUNT_TEST)
        .accountCcy(CURRENCY_USD);
  }

  protected ConfigServiceImpl mockTransactionConfig_NonOTP() {
    try {
      ConfigServiceImpl transactionConfig = new ConfigServiceImpl(null);
      transactionConfig.setJsonValue(
          new JSONObject().put("txMinAmt", 1.0).put("txMaxAmt", 1000.0).put("txOtpRequired", 0));

      return transactionConfig;
    } catch (JSONException ex) {
      return null;
    }
  }

  protected ConfigServiceImpl mockTransactionConfig_RequireOTP() {
    try {
      ConfigServiceImpl transactionConfig = new ConfigServiceImpl(null);
      transactionConfig.setJsonValue(
          new JSONObject().put("txMinAmt", 1.0).put("txMaxAmt", 1000.0).put("txOtpRequired", 1));

      return transactionConfig;
    } catch (JSONException ex) {
      return null;
    }
  }


  protected CDRBGetAccountDetailResponse mockCdrbAccountResponse() {
    return new CDRBGetAccountDetailResponse()
        .acct(
            new CDRBGetAccountDetailResponseAcct()
                .accountNo(CASA_ACCOUNT_TEST)
                .accountType(CasaAccountType.D)
                .accountStatus(CasaAccountStatus._1)
                .accountName("name")
                .cifNo("123")
                .currentBal(1000.0)
                .availBal(1000.0)
                .currencyCode(CURRENCY_USD)
                .ctryCitizen("KH")
                .kycStatus(CasaKYCStatus.F));
  }

  protected PGProfileResponse mockBakongUserProfile() {
    return new PGProfileResponse()
        .accountName("test")
        .accountId("123456xxx")
        .kycStatus(KycStatusEnum.FULL_KYC.getName())
        .phone("855xxx")
        .accountStatus(AccountStatusEnum.ACTIVATED.getStatus());
  }

  protected CDRBFeeAndCashbackResponse mockCDRBFeeAndCashback() {
    return new CDRBFeeAndCashbackResponse().fee(0.2).amount(10.0).currencyCode("USD").cashBack(0.0);
  }
}
