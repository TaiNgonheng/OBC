package com.rhbgroup.dte.obc.domains.transaction.service.impl;

import static com.rhbgroup.dte.obc.common.func.Functions.of;
import static com.rhbgroup.dte.obc.common.func.Functions.peek;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.ObcDateUtils;
import com.rhbgroup.dte.obc.common.util.SFTPUtil;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapper;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapperImpl;
import com.rhbgroup.dte.obc.domains.transaction.model.SIBSBatchTransaction;
import com.rhbgroup.dte.obc.domains.transaction.repository.BatchReportRepository;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionHistoryRepository;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionRepository;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.BatchReport;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionHistoryEntity;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionValidator;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.InternalException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

  private static final String CURRENCY_KHR = "KHR";
  private static final String CURRENCY_USD = "USD";
  private final UserAuthService userAuthService;
  private final ConfigService configService;
  private final AccountService accountService;

  private final PGRestClient pgRestClient;
  private final CDRBRestClient cdrbRestClient;
  private final InfoBipRestClient infoBipRestClient;

  private final TransactionRepository transactionRepository;
  private final BatchReportRepository batchReportRepository;
  private final TransactionHistoryRepository transactionHistoryRepository;

  private final SFTPUtil sftpUtil;
  private final ApplicationProperties applicationProperties;

  private final TransactionMapper transactionMapper = new TransactionMapperImpl();

  private final ApplicationProperties properties;

  @Override
  public void save(TransactionModel transactionModel) {
    of(transactionMapper::toEntity).andThen(transactionRepository::save).apply(transactionModel);
  }

  @Override
  @Transactional
  public InitTransactionResponse initTransaction(InitTransactionRequest request) {
    validateInitTransactionRequest(request);
    CustomUserDetails currentUser = userAuthService.getCurrentUser();

    AccountModel linkedAccount =
        accountService.getActiveAccount(
            new AccountFilterCondition().accountNo(request.getSourceAcc()));

    validateUserAccountWithBakongId(request.getDestinationAcc(), request.getSourceAcc());

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
    AccountValidator.validateAccountStatus(casaAccount);
    AccountValidator.validateBalanceAndCurrency(casaAccount, request);

    // Getting fee and cashback
    CDRBFeeAndCashbackResponse feeAndCashback = getFeeAndCashback(request);

    // Store PENDING transaction
    TransactionModel pendingTransaction =
        transactionMapper.toPendingTransactionModel(
            currentUser.getUserId(),
            request,
            userProfile,
            linkedAccount.getAccountName(),
            feeAndCashback);
    save(pendingTransaction);

    if (properties.isInitTransferRequiredOtp()) {
      infoBipRestClient.sendOtp(currentUser.getPhoneNumber(), currentUser.getBakongId());
    }

    return new InitTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new InitTransactionResponseAllOfData()
                .initRefNumber(pendingTransaction.getInitRefNumber())
                // CDRB-3489 : As a user, when you perform a wallet top up, you want to see how much
                // fee, amount and total amount debited in transaction based currency
                .debitAmount(pendingTransaction.getTrxAmount() + pendingTransaction.getTrxFee())
                .amount(pendingTransaction.getTrxAmount())
                .debitCcy(pendingTransaction.getTrxCcy())
                .requireOtp(properties.isInitTransferRequiredOtp())
                .fee(feeAndCashback.getFee()));
  }

  private void validateUserAccountWithBakongId(String bakongId, String accountId) {
    boolean isAccountLinkedToBakongId =
        accountService.checkAccountLinkedWithBakongId(bakongId, accountId);
    if (!isAccountLinkedToBakongId) {
      throw new BizException(ResponseMessage.ACCOUNT_NOT_LINKED_WITH_BAKONG_ACCOUNT);
    }
  }

  private void validateInitTransactionRequest(InitTransactionRequest request) {
    if (StringUtils.isBlank(request.getType())) {
      throw new BizException(ResponseMessage.MISSING_TRANSFER_TYPE);
    }

    if (!request.getType().equals(TransactionType.CASA.getValue())
        && !request.getType().equals(TransactionType.WALLET.getValue())) {
      throw new BizException(ResponseMessage.INVALID_TRANSFER_TYPE);
    }

    if (StringUtils.isBlank(request.getSourceAcc())) {
      throw new BizException(ResponseMessage.MISSING_SOURCE_ACC);
    }

    if (StringUtils.isBlank(request.getDestinationAcc())) {
      throw new BizException(ResponseMessage.MISSING_DESTINATION_ACC_ID);
    }

    if (request.getAmount() == null) {
      throw new BizException(ResponseMessage.MISSING_AMOUNT);
    }

    if (StringUtils.isBlank(request.getCcy())) {
      throw new BizException(ResponseMessage.MISSING_CCY);
    }

    boolean amountIsDecimal = request.getAmount() % 1 != 0;
    if (request.getCcy().equals(CURRENCY_KHR) && amountIsDecimal) {
      throw new BizException(ResponseMessage.INVALID_AMOUNT);
    }

    if (request.getDesc() != null && request.getDesc().length() > 64) {
      throw new BizException(ResponseMessage.DESC_TOO_LONG);
    }

    if (!request.getCcy().equals(CURRENCY_KHR) && !request.getCcy().equals(CURRENCY_USD)) {
      throw new BizException(ResponseMessage.INVALID_CURRENCY);
    }

    if (overDailyLimit(request)) {
      throw new BizException(ResponseMessage.OVER_DAILY_TRANSFER_LIMIT);
    }
  }

  private boolean overDailyLimit(InitTransactionRequest request) {
    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    BigDecimal todayDebitAmountPerAccount =
        accumulateTodayDebitAmountPerAccount(request.getSourceAcc(), currentUser.getUserId());

    CDRBFeeAndCashbackResponse feeAndCashback = getFeeAndCashback(request);

    Double dailyLimit = getDailyLimitFromConfig(request);

    return todayDebitAmountPerAccount
            .add(BigDecimal.valueOf(request.getAmount()))
            .add(BigDecimal.valueOf(feeAndCashback.getFee()))
            .compareTo(BigDecimal.valueOf(dailyLimit))
        > 0;
  }

  private Double getDailyLimitFromConfig(InitTransactionRequest request) {
    ConfigService transactionConfig =
        configService.loadJSONValue(ConfigConstants.Transaction.mapCurrency(request.getCcy()));
    return transactionConfig.getValue(ConfigConstants.Transaction.DAILY_LIMIT, Double.class);
  }

  private CDRBFeeAndCashbackResponse getFeeAndCashback(InitTransactionRequest request) {
    return cdrbRestClient.getFeeAndCashback(
        new CDRBFeeAndCashbackRequest()
            .amount(request.getAmount())
            .currencyCode(request.getCcy())
            .transactionType(AppConstants.Transaction.OBC_TOP_UP));
  }

  private BigDecimal accumulateTodayDebitAmountPerAccount(String sourceAcc, Long userId) {

    Double todayTotalDebitAmountByAcctId =
        transactionRepository.sumTodayTotalDebitAmountByAcctId(
            sourceAcc, TransactionStatus.COMPLETED.getValue(), LocalDate.now(), userId);
    return todayTotalDebitAmountByAcctId == null
        ? BigDecimal.valueOf(0)
        : BigDecimal.valueOf(todayTotalDebitAmountByAcctId);
  }

  @Override
  @Transactional
  public FinishTransactionResponse finishTransaction(FinishTransactionRequest request) {
    validateFinishTransactionRequest(request);
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
                        () -> new BizException(ResponseMessage.INIT_REFNUMBER_NOT_FOUND)))
            .andThen(peek(TransactionValidator::validateTransactionStatus))
            .andThen(
                peek(
                    entity -> {
                      if (properties.isInitTransferRequiredOtp()
                          && Boolean.FALSE.equals(
                              infoBipRestClient.verifyOtp(
                                  request.getOtpCode(), currentUser.getBakongId()))) {
                        throw new BizException(ResponseMessage.INVALID_OTP);
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
        .andThen(this::setExchangeRateRelatedFields)
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

  CDRBTransferRequest setExchangeRateRelatedFields(CDRBTransferRequest cdrbTransferRequest) {
    if (CURRENCY_KHR.equals(cdrbTransferRequest.getAccountCurrencyCode())
        && CURRENCY_KHR.equals(cdrbTransferRequest.getCurrencyCode())) {
      DecimalFormat df = new DecimalFormat("###.##");
      CDRBGetExchangeRateRequest cdrbGetExchangeRateRequest = new CDRBGetExchangeRateRequest();
      cdrbGetExchangeRateRequest.setCurrencyCode(cdrbTransferRequest.getCurrencyCode());
      cdrbGetExchangeRateRequest.setFrAccountNo(cdrbTransferRequest.getFromAccountNo());
      cdrbGetExchangeRateRequest.setFrAccountType(
          cdrbTransferRequest.getFromAccountType().getValue());
      ExchangeRateResponse exchangeRateResponse =
          cdrbRestClient.fetchExchangeRates(cdrbGetExchangeRateRequest);
      cdrbTransferRequest.setBuyRate(exchangeRateResponse.getMidRate());
      cdrbTransferRequest.setSellRate(exchangeRateResponse.getMidRate());
      cdrbTransferRequest.setFeeAmountInUSD(
          Double.parseDouble(
              df.format(exchangeRateResponse.getMidRate() * cdrbTransferRequest.getFees())));
      cdrbTransferRequest.setTransactionAmountInUSD(
          Double.parseDouble(
              df.format(exchangeRateResponse.getMidRate() * cdrbTransferRequest.getAmount())));
      cdrbTransferRequest.setCashBackAmountInUSD(
          Double.parseDouble(
              df.format(exchangeRateResponse.getMidRate() * cdrbTransferRequest.getCashBack())));
    } else {
      cdrbTransferRequest.setFeeAmountInUSD(cdrbTransferRequest.getFees());
      cdrbTransferRequest.setTransactionAmountInUSD(cdrbTransferRequest.getAmount());
      cdrbTransferRequest.setCashBackAmountInUSD(cdrbTransferRequest.getCashBack());
      cdrbTransferRequest.setBuyRate(1d);
      cdrbTransferRequest.setSellRate(1d);
    }
    return cdrbTransferRequest;
  }

  private void validateFinishTransactionRequest(FinishTransactionRequest request) {
    if (StringUtils.isBlank(request.getInitRefNumber())) {
      throw new BizException(ResponseMessage.MISSING_INITREFNUMBER);
    }

    if (request.getInitRefNumber().length() != 32) {
      throw new BizException(ResponseMessage.INVALID_INITREFNUMBER);
    }

    if (StringUtils.isBlank(request.getOtpCode())
        && applicationProperties.isInitTransferRequiredOtp()) {
      throw new BizException(ResponseMessage.MISSING_OTP_CODE);
    }

    if (!StringUtils.isBlank(request.getOtpCode())
        && !applicationProperties.isInitTransferRequiredOtp()) {
      throw new UserAuthenticationException(ResponseMessage.INVALID_TOKEN);
    }

    if (StringUtils.isBlank(request.getKey())) {
      throw new BizException(ResponseMessage.MISSING_KEY);
    } else {
      String pwdRegex = "^[a-f0-9]{64}$";

      Pattern pwdPattern = Pattern.compile(pwdRegex);
      Matcher pwdMatcher = pwdPattern.matcher(request.getKey());
      if (!pwdMatcher.find()) {
        throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
      }
    }
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

  @Override
  @Transactional
  public GetAccountTransactionsResponse queryTransactionHistory(
      GetAccountTransactionsRequest request) {
    validateGetAccountTransactionsRequest(request);

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    return of(accountService::getActiveAccount)
        .andThen(
            peek(
                model ->
                    updateTodayTransaction(request.getPage(), model.getAccountNo(), currentUser)))
        .andThen(
            accountModel ->
                transactionHistoryRepository.queryByFromAccount(
                    request.getAccNumber(),
                    PageRequest.of(
                        request.getPage() - 1,
                        request.getSize(),
                        Sort.by(Sort.Order.desc("trx_date")))))
        .andThen(
            resultPage -> {
              List<TransactionHistoryModel> items =
                  resultPage
                      .get()
                      .map(transactionMapper::toTransactionHistoryModel)
                      .collect(Collectors.toList());
              return new GetAccountTransactionsResponse()
                  .status(ResponseHandler.ok())
                  .data(
                      new GetAccountTransactionsResponseAllOfData()
                          .transactions(items)
                          .totalElement(resultPage.getTotalElements()));
            })
        .apply(new AccountFilterCondition().accountNo(request.getAccNumber()));
  }

  private void validateGetAccountTransactionsRequest(GetAccountTransactionsRequest request) {
    if (StringUtils.isBlank(request.getAccNumber())) {
      throw new BizException(ResponseMessage.MISSING_ACC_NUMBER);
    }

    if (request.getPage() == null) {
      throw new BizException(ResponseMessage.MISSING_PAGE);
    }

    if (request.getPage() < 0) {
      throw new BizException(ResponseMessage.PAGE_LESS_THAN_ZERO);
    }

    if (request.getSize() == null) {
      throw new BizException(ResponseMessage.MISSING_PAGE_SIZE);
    }

    if (request.getSize() < 1) {
      throw new BizException(ResponseMessage.PAGE_SIZE_LESS_THAN_ONE);
    }
  }

  private void updateTodayTransaction(
      Integer currentPage, String accountNum, CustomUserDetails currentUser) {
    if (currentPage > 1) {
      log.info("Page = {}, skip fetching newly record.", currentPage);
      return;
    }

    SIBSSyncDateConfig sibsSyncDateConfig =
        configService.getByConfigKey(
            AppConstants.Transaction.SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class);

    Integer cleanupRecords;
    if (Boolean.TRUE.equals(sibsSyncDateConfig.getUseSIBSSyncDate())) {
      cleanupRecords =
          transactionHistoryRepository.deleteTodayTransactionByAccountNumber(
              accountNum,
              ObcDateUtils.toLocalDate(
                  sibsSyncDateConfig.getSibsSyncDate(), ObcDateUtils.YYYY_MM_DD_NO_SPACE));
    } else {
      cleanupRecords =
          transactionHistoryRepository.deleteTodayTransactionByAccountNumber(accountNum);
    }
    log.info("Cleaning all the newly added by today record {}", cleanupRecords);

    of(cdrbRestClient::fetchTodayTransactionHistory)
        .andThen(CDRBTransactionHistoryResponse::getTransactions)
        .andThen(
            transactions ->
                transactions.stream()
                    .map(
                        trxHistory -> {
                          trxHistory.setObcUserId(currentUser.getUserId());
                          TransactionHistoryEntity entity =
                              transactionMapper.toTransactionHistoryEntity(trxHistory);
                          // Status is always success for this scenario
                          entity.setTrxStatus(TransactionStatus.COMPLETED);
                          return entity;
                        })
                    .collect(Collectors.toList()))
        .andThen(peek(transactionHistoryRepository::saveAll))
        .apply(
            new CDRBTransactionHistoryRequest()
                .accountNumber(accountNum)
                .cifNumber(currentUser.getCif()));
  }

  @Override
  public void processTransactionHistoryBatchFile(TransactionBatchFileProcessingRequest request) {
    LocalDate date = getProcessingDate(request);
    BatchReport report = batchReportRepository.findByDate(date);
    if (ObjectUtils.isNotEmpty(report) && !report.getStatus().equals(BatchReportStatus.FAILED)) {
      throw new BizException(ResponseMessage.FILE_PROCESSED);
    }
    if (ObjectUtils.isEmpty(report)) {
      report = new BatchReport();
      report.setDate(date);
    }
    report.setStatus(BatchReportStatus.PENDING);
    batchReportRepository.saveAndFlush(report);
    String filename = generateTransactionHistoryFilename(date);
    try (InputStream batchFile =
        new ByteArrayInputStream(sftpUtil.downloadFileFromSFTP(filename))) {
      parseFileAndStoreRecordToDB(batchFile, date);
      report.setErrorMessage(null);
      report.setStatus(BatchReportStatus.COMPLETED);
      batchReportRepository.saveAndFlush(report);
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getStackTrace(e);
      if (StringUtils.isNotBlank(stackTrace)
          && stackTrace.length() > applicationProperties.getMaxStackTraceLength()) {
        stackTrace = stackTrace.substring(0, applicationProperties.getMaxStackTraceLength());
      }
      report.setStatus(BatchReportStatus.FAILED);
      report.setErrorMessage(stackTrace);
      batchReportRepository.saveAndFlush(report);
      log.error("Error happen when processing the batch file", e);
    }
  }

  private LocalDate getProcessingDate(TransactionBatchFileProcessingRequest request) {
    LocalDate date = request.getDate();
    if (request.getDate() == null) {
      SIBSSyncDateConfig sibsSyncDateConfig =
          configService.getByConfigKey(
              AppConstants.Transaction.SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class);
      if (Boolean.TRUE.equals(sibsSyncDateConfig.getUseSIBSSyncDate())) {
        date =
            LocalDate.parse(
                    sibsSyncDateConfig.getSibsSyncDate(),
                    DateTimeFormatter.ofPattern(AppConstants.Transaction.DATE_FORMAT_YYYYMMDD))
                .minusDays(1);
      } else {
        date = LocalDate.now().minusDays(1);
      }
    }
    return date;
  }

  private String generateTransactionHistoryFilename(LocalDate date) {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern(AppConstants.Transaction.DATE_FORMAT_DDMMYYYY);
    return AppConstants.Transaction.TRANSACTION_FILE_PREFIX
        + formatter.format(date)
        + AppConstants.Transaction.TRANSACTION_FILE_EXTENSION;
  }

  private void parseFileAndStoreRecordToDB(InputStream is, LocalDate date) throws IOException {
    CsvMapper csvMapper = new CsvMapper();
    CsvSchema csvSchema = csvMapper.typedSchemaFor(SIBSBatchTransaction.class).withHeader();
    MappingIterator<SIBSBatchTransaction> transactionIterator =
        new CsvMapper()
            .readerFor(SIBSBatchTransaction.class)
            .with(csvSchema.withColumnSeparator('|'))
            .readValues(is);
    List<SIBSBatchTransaction> batchTransactions = transactionIterator.readAll();
    List<TransactionHistoryEntity> transactions =
        transactionMapper.toTransactionHistories(
            batchTransactions.stream()
                .filter(
                    r -> r.getRecordType().equals(AppConstants.Transaction.RECORD_DETAIL_INDICATOR))
                .collect(Collectors.toList()));
    transactionHistoryRepository.deleteAllByTrxDate(date);
    transactionHistoryRepository.saveAll(transactions);
  }
}
