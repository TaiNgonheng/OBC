package com.rhbgroup.dte.obc.transaction.service;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionHistoryRepository;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionRepository;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import com.rhbgroup.dte.obc.domains.transaction.service.impl.TransactionServiceImpl;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.BakongAccountType;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBTransactionHistoryResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponse;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.SIBSSyncDateConfig;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.transaction.AbstractTransactionTest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest extends AbstractTransactionTest {
  @InjectMocks TransactionServiceImpl transactionService;

  @Mock TransactionRepository transactionRepository;

  @Mock TransactionHistoryRepository historyRepository;

  @Mock UserAuthService userAuthService;

  @Mock ConfigService configService;

  @Mock PGRestClient pgRestClient;

  @Mock CDRBRestClient cdrbRestClient;

  @Mock AccountService accountService;

  @Mock InfoBipRestClient infoBipRestClient;

  @Mock private ApplicationProperties properties;

  @Test
  void testInitTransaction_Success_CASA_TO_WALLET() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(false);

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());
    when(pgRestClient.getUserProfile(any())).thenReturn(mockBakongUserProfile());
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    CDRBFeeAndCashbackResponse mockFeeAndCashback = mockCDRBFeeAndCashback();
    when(cdrbRestClient.getFeeAndCashback(any())).thenReturn(mockFeeAndCashback);

    when(transactionRepository.save(any())).thenReturn(new TransactionEntity());

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    InitTransactionResponse initTransactionResponse =
        transactionService.initTransaction(initTransactionRequest);
    assertNotNull(initTransactionResponse);

    verify(pgRestClient, times(1)).getUserProfile(any());

    assertNotNull(initTransactionResponse.getData().getInitRefNumber());
    assertFalse(initTransactionResponse.getData().getRequireOtp());

    assertEquals(mockFeeAndCashback.getFee(), initTransactionResponse.getData().getFee());
    assertEquals(
        initTransactionRequest.getAmount(), initTransactionResponse.getData().getDebitAmount());
    assertEquals(initTransactionRequest.getCcy(), initTransactionResponse.getData().getDebitCcy());
  }

  @Test
  void testInitTransaction_OperationNotSupported_CASA_TO_CASA() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(false);
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());
    when(pgRestClient.getUserProfile(any())).thenReturn(mockBakongUserProfile());
    when(cdrbRestClient.getFeeAndCashback(any())).thenReturn(mockCDRBFeeAndCashback());
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    // Update transaction type to CASA_TO_CASA
    initTransactionRequest.setType(TransactionType.CASA.getValue());

    try {
      transactionService.initTransaction(initTransactionRequest);
    } catch (BizException ex) {
      assertEquals(
          ResponseMessage.MANDATORY_FIELD_MISSING.getCode(), ex.getResponseMessage().getCode());
      assertEquals(
          ResponseMessage.MANDATORY_FIELD_MISSING.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitTransaction_Failed_SourceAccNotFound() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    // Return a CASA account which is not matched with request source account
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModelSourceAccNotMatched());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    try {
      InitTransactionResponse initTransactionResponse =
          transactionService.initTransaction(initTransactionRequest);
      assertNotNull(initTransactionResponse);

    } catch (BizException ex) {
      assertEquals(ResponseMessage.NO_ACCOUNT_FOUND.getCode(), ex.getResponseMessage().getCode());
      assertEquals(ResponseMessage.NO_ACCOUNT_FOUND.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitTransaction_Failed_TransferAmtExceedLimitation() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    // Mimic a huge transfer amount
    initTransactionRequest.setAmount(100000.0);
    try {
      InitTransactionResponse initTransactionResponse =
          transactionService.initTransaction(initTransactionRequest);
      assertNotNull(initTransactionResponse);

    } catch (BizException ex) {
      assertEquals(
          ResponseMessage.TRANSACTION_EXCEED_AMOUNT_LIMIT.getCode(),
          ex.getResponseMessage().getCode());
      assertEquals(
          ResponseMessage.TRANSACTION_EXCEED_AMOUNT_LIMIT.getMsg(),
          ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitTransaction_Failed_CurrencyNotMatchWithCasaAccount() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_KHR))
        .thenReturn(mockTransactionConfig());
    when(pgRestClient.getUserProfile(any())).thenReturn(mockBakongUserProfile());
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    // Mimic a different transfer currency
    initTransactionRequest.setCcy("KHR");
    try {
      InitTransactionResponse initTransactionResponse =
          transactionService.initTransaction(initTransactionRequest);
      assertNotNull(initTransactionResponse);

    } catch (BizException ex) {
      assertEquals(ResponseMessage.INVALID_CURRENCY.getCode(), ex.getResponseMessage().getCode());
      assertEquals(ResponseMessage.INVALID_CURRENCY.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitTransaction_Failed_NotEnoughBalance() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());
    when(pgRestClient.getUserProfile(any())).thenReturn(mockBakongUserProfile());
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    // Mimic CASA account has run out of balance
    CDRBGetAccountDetailResponse casaAccDetail = mockCdrbAccountResponse();
    casaAccDetail.getAcct().setCurrentBal(1.0);
    casaAccDetail.getAcct().setAvailBal(1.0);
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(casaAccDetail);

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    initTransactionRequest.setAmount(10.0);
    try {
      InitTransactionResponse initTransactionResponse =
          transactionService.initTransaction(initTransactionRequest);
      assertNotNull(initTransactionResponse);

    } catch (BizException ex) {
      assertEquals(ResponseMessage.BALANCE_NOT_ENOUGH.getCode(), ex.getResponseMessage().getCode());
      assertEquals(ResponseMessage.BALANCE_NOT_ENOUGH.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitTransaction_Failed_TransferToUnavailableSource() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());
    when(pgRestClient.getUserProfile(any()))
        .thenThrow(new BizException(ResponseMessage.TRANSACTION_TO_UNAVAILABLE_ACCOUNT));

    try {
      InitTransactionResponse initTransactionResponse =
          transactionService.initTransaction(mockInitTransactionRequest());
      assertNotNull(initTransactionResponse);

    } catch (BizException ex) {
      assertEquals(
          ResponseMessage.TRANSACTION_TO_UNAVAILABLE_ACCOUNT.getCode(),
          ex.getResponseMessage().getCode());
      assertEquals(
          ResponseMessage.TRANSACTION_TO_UNAVAILABLE_ACCOUNT.getMsg(),
          ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitTransaction_Success_OtpRequired() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(true);
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(true);
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockTransactionConfig());

    when(pgRestClient.getUserProfile(any())).thenReturn(mockBakongUserProfile());
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    CDRBFeeAndCashbackResponse mockFeeAndCashback = mockCDRBFeeAndCashback();
    when(cdrbRestClient.getFeeAndCashback(any())).thenReturn(mockFeeAndCashback);

    when(transactionRepository.save(any())).thenReturn(new TransactionEntity());

    InitTransactionRequest initTransactionRequest = mockInitTransactionRequest();
    InitTransactionResponse initTransactionResponse =
        transactionService.initTransaction(initTransactionRequest);
    assertNotNull(initTransactionResponse);

    verify(pgRestClient, times(1)).getUserProfile(any());
    verify(infoBipRestClient, times(1)).sendOtp(anyString(), anyString());

    assertNotNull(initTransactionResponse.getData().getInitRefNumber());
    assertTrue(initTransactionResponse.getData().getRequireOtp());

    assertEquals(mockFeeAndCashback.getFee(), initTransactionResponse.getData().getFee());
    assertEquals(
        initTransactionRequest.getAmount(), initTransactionResponse.getData().getDebitAmount());
    assertEquals(initTransactionRequest.getCcy(), initTransactionResponse.getData().getDebitCcy());
  }

  @Test
  void testFinishTransaction_Success_OTPNotIncluded() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(false);
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(transactionRepository.findByInitRefNumber(anyString()))
        .thenReturn(Optional.of(mockTransactionEntity(TransactionStatus.PENDING)));

    when(accountService.getAccountDetail(any()))
        .thenReturn(
            new GetAccountDetailResponse()
                .status(ResponseHandler.ok())
                .data(
                    new GetAccountDetailResponseAllOfData()
                        .accCcy("USD")
                        .accType(BakongAccountType.S)));

    when(cdrbRestClient.transfer(any())).thenReturn(mockCDRBTransferResponse());

    CDRBTransferInquiryResponse transactionDetails = mockCDRBTransferDetail();
    when(cdrbRestClient.getTransferDetail(any())).thenReturn(transactionDetails);
    when(transactionRepository.save(any())).thenReturn(new TransactionEntity());
    when(configService.getByConfigKey(ConfigConstants.Transaction.TRX_QUERY_MAX_DURATION, "value"))
        .thenReturn("40");

    FinishTransactionResponse response =
        transactionService.finishTransaction(mockFinishTransactionRequest());

    assertNotNull(response.getStatus());
    assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());

    assertNotNull(response.getData());
    assertEquals(
        transactionDetails.getExternalSystemRef(), response.getData().getTransactionHash());
    assertEquals(transactionDetails.getCorrelationId(), response.getData().getTransactionId());
    assertEquals(
        BigDecimal.valueOf(
            transactionDetails.getTransferCompletionDate().toInstant().toEpochMilli()),
        response.getData().getTransactionDate());
  }

  @Test
  void testFinishTransaction_Success_OTPIncluded() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(true);
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(transactionRepository.findByInitRefNumber(anyString()))
        .thenReturn(Optional.of(mockTransactionEntity(TransactionStatus.PENDING)));

    when(infoBipRestClient.verifyOtp(anyString(), anyString())).thenReturn(true);

    when(accountService.getAccountDetail(any()))
        .thenReturn(
            new GetAccountDetailResponse()
                .status(ResponseHandler.ok())
                .data(
                    new GetAccountDetailResponseAllOfData()
                        .accCcy("USD")
                        .accType(BakongAccountType.S)));

    when(cdrbRestClient.transfer(any())).thenReturn(mockCDRBTransferResponse());

    CDRBTransferInquiryResponse transactionDetails = mockCDRBTransferDetail();
    when(cdrbRestClient.getTransferDetail(any())).thenReturn(transactionDetails);
    when(transactionRepository.save(any())).thenReturn(new TransactionEntity());
    when(configService.getByConfigKey(ConfigConstants.Transaction.TRX_QUERY_MAX_DURATION, "value"))
        .thenReturn("40");

    FinishTransactionRequest requestWithOTP = mockFinishTransactionRequest();
    requestWithOTP.setOtpCode("123456");
    FinishTransactionResponse response = transactionService.finishTransaction(requestWithOTP);

    assertNotNull(response.getStatus());
    assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());

    assertNotNull(response.getData());
    assertEquals(
        transactionDetails.getExternalSystemRef(), response.getData().getTransactionHash());
    assertEquals(transactionDetails.getCorrelationId(), response.getData().getTransactionId());
    assertEquals(
        BigDecimal.valueOf(
            transactionDetails.getTransferCompletionDate().toInstant().toEpochMilli()),
        response.getData().getTransactionDate());
  }

  @Test
  void testFinishTransaction_Failed_AuthenticationError() {

    when(userAuthService.getCurrentUser())
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    try {
      transactionService.finishTransaction(mockFinishTransactionRequest());
    } catch (UserAuthenticationException ex) {
      assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getCode(), ex.getResponseMessage().getCode());
      assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishTransaction_Failed_InputPasswordNotCorrect() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userAuthService.authenticate(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    try {
      transactionService.finishTransaction(mockFinishTransactionRequest());
    } catch (UserAuthenticationException ex) {
      assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getCode(), ex.getResponseMessage().getCode());
      assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishTransaction_Failed_InitRefNumberNotFound() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(transactionRepository.findByInitRefNumber(anyString())).thenReturn(Optional.empty());

    try {
      transactionService.finishTransaction(mockFinishTransactionRequest());
    } catch (BizException ex) {
      assertEquals(
          ResponseMessage.INIT_REFNUMBER_NOT_FOUND.getCode(), ex.getResponseMessage().getCode());
      assertEquals(
          ResponseMessage.INIT_REFNUMBER_NOT_FOUND.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishTransaction_Failed_InvalidOTPToken() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(true);
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(transactionRepository.findByInitRefNumber(anyString()))
        .thenReturn(Optional.of(mockTransactionEntity(TransactionStatus.PENDING)));

    when(infoBipRestClient.verifyOtp(anyString(), anyString())).thenReturn(false);
    FinishTransactionRequest requestWithOTP = mockFinishTransactionRequest();
    requestWithOTP.setOtpCode("123456");
    try {
      transactionService.finishTransaction(requestWithOTP);
    } catch (BizException ex) {
      assertEquals(ResponseMessage.INVALID_TOKEN.getCode(), ex.getResponseMessage().getCode());
      assertEquals(ResponseMessage.INVALID_TOKEN.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishTransaction_Failed_CoreTransferError() {
    when(properties.isInitTransferRequiredOtp()).thenReturn(false);
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(transactionRepository.findByInitRefNumber(anyString()))
        .thenReturn(Optional.of(mockTransactionEntity(TransactionStatus.PENDING)));

    when(accountService.getAccountDetail(any()))
        .thenReturn(
            new GetAccountDetailResponse()
                .status(ResponseHandler.ok())
                .data(
                    new GetAccountDetailResponseAllOfData()
                        .accCcy("USD")
                        .accType(BakongAccountType.S)));

    when(cdrbRestClient.transfer(any())).thenReturn(mockCDRBTransferResponse());
    when(configService.getByConfigKey(ConfigConstants.Transaction.TRX_QUERY_MAX_DURATION, "value"))
        .thenReturn("40");
    CDRBTransferInquiryResponse transactionDetails = mockCDRBTransferDetailError();
    when(cdrbRestClient.getTransferDetail(any())).thenReturn(transactionDetails);
    when(transactionRepository.save(any())).thenReturn(new TransactionEntity());

    FinishTransactionResponse response =
        transactionService.finishTransaction(mockFinishTransactionRequest());

    assertNotNull(response.getData());
    assertNull(response.getData().getTransactionDate());
    assertNull(response.getData().getTransactionHash());
  }

  @Test
  void testQueryAccountTransaction_Success_NoNewRecordToday() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());
    when(cdrbRestClient.fetchTodayTransactionHistory(any()))
        .thenReturn(new CDRBTransactionHistoryResponse().transactions(Collections.emptyList()));
    when(historyRepository.queryByFromAccount(anyString(), any())).thenReturn(mockTrxHistoryPage());
    when(configService.getByConfigKey(
            AppConstants.Transaction.SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class))
        .thenReturn(new SIBSSyncDateConfig().useSIBSSyncDate(false).sibsSyncDate("20230729"));

    GetAccountTransactionsRequest mockRequest = mockAccountTransactionRequest();
    GetAccountTransactionsResponse response =
        transactionService.queryTransactionHistory(mockRequest);

    assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    assertNotNull(response.getData());
    assertEquals(
        response.getData().getTransactions().size(),
        response.getData().getTotalElement().intValue());
  }

  @Test
  void testQueryAccountTransaction_Success_Have2NewRecordToday() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());

    // Update this one to return 2 more results
    when(cdrbRestClient.fetchTodayTransactionHistory(any())).thenReturn(mock2MoreRecordsToday());
    when(historyRepository.queryByFromAccount(anyString(), any())).thenReturn(mockTrxHistoryPage());
    when(configService.getByConfigKey(
            AppConstants.Transaction.SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class))
        .thenReturn(new SIBSSyncDateConfig().useSIBSSyncDate(true).sibsSyncDate("2023-07-29"));

    GetAccountTransactionsRequest mockRequest = mockAccountTransactionRequest();
    GetAccountTransactionsResponse response =
        transactionService.queryTransactionHistory(mockRequest);

    assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    assertNotNull(response.getData());
    assertEquals(
        response.getData().getTransactions().size(),
        response.getData().getTotalElement().intValue());
  }

  @Test
  void testQueryAccountTransaction_Success_PageRequestLargerThanOne() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.getActiveAccount(any())).thenReturn(mockAccountModel());

    when(historyRepository.queryByFromAccount(anyString(), any())).thenReturn(mockTrxHistoryPage());

    GetAccountTransactionsRequest mockRequest = mockAccountTransactionRequest();
    mockRequest.setPage(2);
    GetAccountTransactionsResponse response =
        transactionService.queryTransactionHistory(mockRequest);

    verify(cdrbRestClient, times(0)).fetchTodayTransactionHistory(any());

    assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    assertNotNull(response.getData());
    assertEquals(
        response.getData().getTransactions().size(),
        response.getData().getTotalElement().intValue());
  }

  @Test
  void testInitTransactionWithAccountDoesNotLinkWithBakongId() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountService.checkAccountLinkedWithBakongId(anyString(), anyString())).thenReturn(false);
    BizException execption =
        catchThrowableOfType(
            () -> transactionService.initTransaction(mockInitTransactionRequest()),
            BizException.class);

    assertEquals(
        ResponseMessage.ACCOUNT_NOT_LINKED_WITH_BAKONG_ACCOUNT.getCode(),
        execption.getResponseMessage().getCode());
    assertEquals(
        ResponseMessage.ACCOUNT_NOT_LINKED_WITH_BAKONG_ACCOUNT.getMsg(),
        execption.getResponseMessage().getMsg());
  }
}
