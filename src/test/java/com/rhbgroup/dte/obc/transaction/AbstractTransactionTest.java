package com.rhbgroup.dte.obc.transaction;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.common.util.RandomGenerator;
import com.rhbgroup.dte.obc.domains.config.service.impl.ConfigServiceImpl;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionHistoryEntity;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponseAcct;
import com.rhbgroup.dte.obc.model.CDRBTransactionHistoryResponse;
import com.rhbgroup.dte.obc.model.CDRBTransactionHistoryResponseTransactions;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferResponse;
import com.rhbgroup.dte.obc.model.CasaAccountStatus;
import com.rhbgroup.dte.obc.model.CasaAccountType;
import com.rhbgroup.dte.obc.model.CasaKYCStatus;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponse;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponseAllOfData;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.TransactionHistoryModel;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.codehaus.plexus.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public abstract class AbstractTransactionTest {

  private static final String CASA_ACCOUNT_TEST = "123xxx";
  private static final String BAKONG_ACCOUNT_TEST = "bakong@oski";
  private static final String CURRENCY_USD = "USD";
  private static final String PHONE_NUMBER = "85500000000";
  private static final Double TRX_AMOUNT = 1.0;
  private static final String TRX_REF = "Hj8ecgOtDHpZhqx20NbmPtrpaXRkS35f";
  private static final String CORRELATION_ID = "414d5120444b484344524230322020205f9f4d610e766827";

  protected InitTransactionRequest mockInitTransactionRequest() {
    return new InitTransactionRequest()
        .type(TransactionType.WALLET.getValue())
        .sourceAcc(CASA_ACCOUNT_TEST)
        .destinationAcc(BAKONG_ACCOUNT_TEST)
        .amount(TRX_AMOUNT)
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
                .debitAmount(TRX_AMOUNT));
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

  protected ConfigServiceImpl mockTransactionConfig() {
    try {
      ConfigServiceImpl transactionConfig = new ConfigServiceImpl(null);
      transactionConfig.setJsonValue(new JSONObject().put("txMinAmt", 1.0).put("txMaxAmt", 1000.0));

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

  protected FinishTransactionRequest mockFinishTransactionRequest() {
    return new FinishTransactionRequest()
        .initRefNumber(TRX_REF)
        .key("54b688a517f7654563a6c64d945a3670880a4c602ec67a065bbebbcd2b22edd5");
  }

  protected FinishTransactionResponse mockFinishTransactionResponse() {
    return new FinishTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new FinishTransactionResponseAllOfData()
                .transactionHash("123456")
                .transactionId("123")
                .transactionDate(BigDecimal.valueOf(Instant.now().toEpochMilli())));
  }

  protected TransactionEntity mockTransactionEntity(TransactionStatus status) {
    TransactionEntity entity = new TransactionEntity();
    entity.setTrxStatus(status);
    entity.setFromAccount(CASA_ACCOUNT_TEST);
    entity.setToAccount(BAKONG_ACCOUNT_TEST);
    entity.setTrxAmount(TRX_AMOUNT);
    entity.setInitRefNumber(TRX_REF);
    entity.setTrxCcy(CURRENCY_USD);
    entity.setTrxFee(1.0);
    entity.setTrxCashback(0.0);

    return entity;
  }

  protected CDRBTransferResponse mockCDRBTransferResponse() {
    return new CDRBTransferResponse().correlationId(CORRELATION_ID);
  }

  protected CDRBTransferInquiryResponse mockCDRBTransferDetail() {
    return new CDRBTransferInquiryResponse()
        .correlationId(CORRELATION_ID)
        .status("COMPLETED")
        .externalSystemRef("transaction-hash")
        .transferCompletionDate(OffsetDateTime.now());
  }

  protected CDRBTransferInquiryResponse mockCDRBTransferDetailError() {
    return new CDRBTransferInquiryResponse()
        .correlationId(CORRELATION_ID)
        .status("FAILED")
        .externalSystemRef(null)
        .transferCompletionDate(null);
  }

  protected GetAccountTransactionsRequest mockAccountTransactionRequest() {
    return new GetAccountTransactionsRequest().page(1).accNumber(CASA_ACCOUNT_TEST).size(10);
  }

  protected GetAccountTransactionsResponse mockAccountTransactionResponse() {
    return new GetAccountTransactionsResponse()
        .status(ResponseHandler.ok())
        .data(
            new GetAccountTransactionsResponseAllOfData()
                .totalElement(2L)
                .transactions(
                    Arrays.asList(
                        new TransactionHistoryModel()
                            .transactionId("1")
                            .sourceAcc(CASA_ACCOUNT_TEST)
                            .destinationAcc(BAKONG_ACCOUNT_TEST)
                            .amount(TRX_AMOUNT)
                            .ccy(CURRENCY_USD),
                        new TransactionHistoryModel()
                            .transactionId("2")
                            .sourceAcc(CASA_ACCOUNT_TEST)
                            .destinationAcc(BAKONG_ACCOUNT_TEST)
                            .amount(TRX_AMOUNT)
                            .ccy(CURRENCY_USD))));
  }

  protected Page<TransactionHistoryEntity> mockTrxHistoryPage() {

    TransactionHistoryEntity entity1 = new TransactionHistoryEntity();
    entity1.setTrxId("1");
    entity1.setTrxStatus(TransactionStatus.FAILED);

    TransactionHistoryEntity entity2 = new TransactionHistoryEntity();
    entity2.setTrxId("2");
    entity2.setTrxStatus(TransactionStatus.COMPLETED);

    return new PageImpl<>(Arrays.asList(entity1, entity2));
  }

  protected CDRBTransactionHistoryResponse mock2MoreRecordsToday() {
    return new CDRBTransactionHistoryResponse()
        .transactions(
            Arrays.asList(
                new CDRBTransactionHistoryResponseTransactions()
                    .transactionDate("01012023")
                    .transactionTime(135623L),
                new CDRBTransactionHistoryResponseTransactions()
                    .transactionDate("01012023")
                    .transactionTime(123456L)));
  }
}
