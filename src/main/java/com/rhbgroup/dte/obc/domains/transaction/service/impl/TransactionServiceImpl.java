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
import com.rhbgroup.dte.obc.model.AccountFilterCondition;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.BatchReportStatus;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackRequest;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBTransactionHistoryRequest;
import com.rhbgroup.dte.obc.model.CDRBTransactionHistoryResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryRequest;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponse;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponseAllOfData;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.SIBSSyncDateConfig;
import com.rhbgroup.dte.obc.model.TransactionBatchFileProcessingRequest;
import com.rhbgroup.dte.obc.model.TransactionHistoryModel;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
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

  private final UserAuthService userAuthService;
  private final ConfigService configService;
  private final AccountService accountService;
  private final TransactionRepository transactionRepository;
  private final TransactionHistoryRepository transactionHistoryRepository;
  private final PGRestClient pgRestClient;
  private final CDRBRestClient cdrbRestClient;
  private final InfoBipRestClient infoBipRestClient;
  private final TransactionMapper transactionMapper = new TransactionMapperImpl();
  private final SFTPUtil sftpUtil;
  private final BatchReportRepository batchReportRepository;
  private final ApplicationProperties applicationProperties;

  private static final String TRANSACTION_FILE_PREFIX = "OBCDailyTrx_";
  private static final String DATE_FORMAT_DDMMYYYY = "ddMMyyyy";
  private static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
  private static final String TRANSACTION_FILE_EXTENSION = ".csv";
  private static final String SIBS_SYNC_DATE_KEY = "SIBS_DATE_CONFIG";

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
            getTrxRequest -> transactionInquiryRecursive(getTrxRequest, request.getInitRefNumber()))
        .andThen(transactionMapper::toFinishTransactionResponse)
        .apply(transaction);
  }

  private CDRBTransferInquiryResponse transactionInquiryRecursive(
      CDRBTransferInquiryRequest request, String initRef) {

    CDRBTransferInquiryResponse response = cdrbRestClient.getTransferDetail(request);
    if (TransactionStatus.PENDING.getValue().equals(response.getStatus())) {
      // Retry interval is 1s
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      return transactionInquiryRecursive(request, initRef);
    }

    transactionRepository
        .findByInitRefNumber(initRef)
        .ifPresent(
            trx -> {
              trx.setTrxStatus(TransactionStatus.fromValue(response.getStatus()));
              transactionRepository.save(trx);
            });

    return response;
  }

  @Override
  @Transactional
  public GetAccountTransactionsResponse queryTransactionHistory(
      GetAccountTransactionsRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    return of(accountService::getActiveAccount)
        .andThen(
            peek(
                model ->
                    updateTodayTransaction(
                        request.getPage(), model.getAccountNo(), currentUser.getCif())))
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

  private void updateTodayTransaction(Integer currentPage, String accountNum, String cif) {
    if (currentPage > 1) {
      log.info("Page = {}, skip fetching newly record.", currentPage);
      return;
    }

    Integer refreshedRecords =
        transactionHistoryRepository.deleteTodayTransactionByAccountNumber(accountNum);
    log.info("Cleaning all the newly added by today record {}", refreshedRecords);

    of(cdrbRestClient::fetchTodayTransactionHistory)
        .andThen(CDRBTransactionHistoryResponse::getTransactions)
        .andThen(
            transactions ->
                transactions.stream()
                    .map(transactionMapper::toTransactionHistoryEntity)
                    .collect(Collectors.toList()))
        .andThen(peek(transactionHistoryRepository::saveAll))
        .apply(new CDRBTransactionHistoryRequest().accountNumber(accountNum).cifNumber(cif));
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
          configService.getByConfigKey(SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class);
      if (Boolean.TRUE.equals(sibsSyncDateConfig.getUseSIBSSyncDate())) {
        date =
            LocalDate.parse(
                    sibsSyncDateConfig.getSibsSyncDate(),
                    DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD))
                .minusDays(1);
      } else {
        date = LocalDate.now().minusDays(1);
      }
    }
    return date;
  }

  private String generateTransactionHistoryFilename(LocalDate date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY);
    return TRANSACTION_FILE_PREFIX + formatter.format(date) + TRANSACTION_FILE_EXTENSION;
  }

  private void parseFileAndStoreRecordToDB(InputStream is, LocalDate date) throws IOException {
    CsvMapper csvMapper = new CsvMapper();
    CsvSchema csvSchema = csvMapper.typedSchemaFor(SIBSBatchTransaction.class).withHeader();
    MappingIterator<SIBSBatchTransaction> transactionIterator =
        new CsvMapper()
            .readerFor(SIBSBatchTransaction.class)
            .with(csvSchema.withColumnSeparator(','))
            .readValues(is);
    List<SIBSBatchTransaction> batchTransactions = transactionIterator.readAll();
    List<TransactionHistoryEntity> transactions =
        transactionMapper.toTransactionHistories(batchTransactions);
    transactionHistoryRepository.deleteAllByTrxDate(
        transactionMapper.getInstantFromLocalDate(date));
    transactionHistoryRepository.saveAll(transactions);
  }
}
