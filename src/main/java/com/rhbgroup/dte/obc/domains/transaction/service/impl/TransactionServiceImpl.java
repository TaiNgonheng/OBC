package com.rhbgroup.dte.obc.domains.transaction.service.impl;

import static com.rhbgroup.dte.obc.common.func.Functions.of;
import static com.rhbgroup.dte.obc.common.func.Functions.peek;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapper;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapperImpl;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionRepository;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionValidator;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.InternalException;
import com.rhbgroup.dte.obc.model.AccountFilterCondition;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackRequest;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryRequest;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.time.Instant;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

  private final UserAuthService userAuthService;
  private final ConfigService configService;
  private final AccountService accountService;

  private final TransactionRepository transactionRepository;

  private final PGRestClient pgRestClient;
  private final CDRBRestClient cdrbRestClient;
  private final InfoBipRestClient infoBipRestClient;

  private final TransactionMapper transactionMapper = new TransactionMapperImpl();

  @Override
  public void save(TransactionModel transactionModel) {
    of(transactionMapper::toEntity).andThen(transactionRepository::save).apply(transactionModel);
  }

  @Override
  @Transactional
  public InitTransactionResponse initTransaction(InitTransactionRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();

    AccountModel linkedAccount =
        accountService.getActiveAccount(
            new AccountFilterCondition().accountNo(request.getSourceAcc()));

    ConfigService transactionConfig =
        this.configService.loadJSONValue(ConfigConstants.Transaction.mapCurrency(request.getCcy()));

    // Validate transaction request
    TransactionValidator.validateTransactionLimit(request, transactionConfig, linkedAccount);

    PGProfileResponse userProfile;
    try {
      userProfile =
          pgRestClient.getUserProfile(Collections.singletonList(request.getDestinationAcc()));

      // Validate destination account
      AccountValidator.validateAccount(userProfile);

    } catch (BizException ex) {
      throw new BizException(ResponseMessage.TRANSACTION_TO_UNAVAILABLE_ACCOUNT);
    }

    // Validate CASA account balance
    CDRBGetAccountDetailResponse casaAccount =
        cdrbRestClient.getAccountDetail(
            new CDRBGetAccountDetailRequest()
                .accountNo(request.getSourceAcc())
                .cifNo(currentUser.getCif()));
    AccountValidator.validateBalanceAndCurrency(casaAccount, request);

    // Getting fee and cashback
    CDRBFeeAndCashbackResponse feeAndCashback =
        cdrbRestClient.getFeeAndCashback(
            new CDRBFeeAndCashbackRequest()
                .amount(request.getAmount())
                .currencyCode(request.getCcy())
                .transactionType(AppConstants.Transaction.OBC_TOP_UP));

    // Store PENDING transaction
    TransactionModel pendingTransaction =
        transactionMapper.toPendingTransactionModel(
            currentUser.getUserId(),
            request,
            userProfile,
            linkedAccount.getAccountName(),
            feeAndCashback);
    save(pendingTransaction);

    // Get otp required config
    boolean trxOtpEnabled =
        transactionConfig.getValue(ConfigConstants.Transaction.OTP_REQUIRED, Integer.class) == 1;

    if (trxOtpEnabled) {
      infoBipRestClient.sendOtp(currentUser.getPhoneNumber(), currentUser.getBakongId());
    }

    return new InitTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new InitTransactionResponseAllOfData()
                .initRefNumber(pendingTransaction.getInitRefNumber())
                .debitAmount(pendingTransaction.getTrxAmount())
                .debitCcy(pendingTransaction.getTrxCcy())
                .requireOtp(trxOtpEnabled)
                .fee(feeAndCashback.getFee()));
  }

  @Override
  @Transactional
  public FinishTransactionResponse finishTransaction(FinishTransactionRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    // Authenticate again to confirm password
    userAuthService.authenticate(
        new UserModel().username(currentUser.getUsername()).password(request.getKey()));

    // Validate transaction & OTP
    TransactionEntity transaction =
        of(transactionRepository::findByInitRefNumber)
            .andThen(
                trxOptional ->
                    trxOptional.orElseThrow(
                        () -> new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR)))
            .andThen(peek(TransactionValidator::validateTransactionStatus))
            .andThen(
                peek(
                    entity -> {
                      ConfigService transactionConfig =
                          this.configService.loadJSONValue(
                              ConfigConstants.Transaction.mapCurrency(entity.getTrxCcy()));
                      // Verify OTP
                      boolean otpRequired =
                          transactionConfig.getValue(
                                  ConfigConstants.Transaction.OTP_REQUIRED, Integer.class)
                              == 1;
                      if (otpRequired
                          && Boolean.FALSE.equals(
                              infoBipRestClient.verifyOtp(
                                  request.getOtpCode(), currentUser.getBakongId()))) {
                        throw new BizException(ResponseMessage.INVALID_TOKEN);
                      }
                    }))
            .apply(request.getInitRefNumber());

    // Execute transfer
    return of(transactionMapper::toCDRBTransferRequestBaseMapping)
        .andThen(
            transferRequest ->
                transactionMapper.toCDRBTransferRequestAdditionalMapping(
                    transferRequest,
                    currentUser,
                    accountService.getAccountDetail(
                        new GetAccountDetailRequest()
                            .accNumber(transferRequest.getFromAccountNo()))))
        .andThen(cdrbRestClient::transfer)
        .andThen(
            transferResponse ->
                new CDRBTransferInquiryRequest().correlationId(transferResponse.getCorrelationId()))
        .andThen(
            getTrxRequest -> {
              String maxDurationInSec =
                  configService.getByConfigKey(
                      ConfigConstants.Transaction.TRX_QUERY_MAX_DURATION, "value");
              return transactionInquiryRecursive(
                  getTrxRequest,
                  request.getInitRefNumber(),
                  Integer.parseInt(maxDurationInSec),
                  Instant.now().toEpochMilli());
            })
        .andThen(transactionMapper::toFinishTransactionResponse)
        .apply(transaction);
  }

  private CDRBTransferInquiryResponse transactionInquiryRecursive(
      CDRBTransferInquiryRequest request,
      String initRef,
      Integer maxDurationInSec,
      Long intTrxTime) {

    CDRBTransferInquiryResponse response = cdrbRestClient.getTransferDetail(request);
    if (TransactionStatus.PENDING.getValue().equals(response.getStatus())) {
      try {
        long interval = calculateInterval(maxDurationInSec, intTrxTime);
        log.info("Interval = {}", interval);
        Thread.sleep(interval);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      return transactionInquiryRecursive(request, initRef, maxDurationInSec, intTrxTime);
    }
    transactionRepository
        .findByInitRefNumber(initRef)
        .ifPresent(
            trx -> {
              trx.setTrxStatus(TransactionStatus.fromValue(response.getStatus()));
              trx.setTrxCompletionDate(Instant.now());
              transactionRepository.save(trx);
            });

    return response;
  }

  private long calculateInterval(Integer maxDurationInSec, Long initTrxTime) {

    long timeRange = Instant.now().toEpochMilli() - initTrxTime;
    if (timeRange <= 30 * 1000) {
      log.info("Time range is less than 30s");
      return 1000L;

    } else if (timeRange <= maxDurationInSec * 1000L) {
      log.info("Time range is less than 40s");
      return 5000L;

    } else {
      log.info("Maximum time range has been exceeded");
      throw new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
  }
}
