package com.rhbgroup.dte.obc.domains.transaction.service.impl;

import static com.rhbgroup.dte.obc.common.func.Functions.of;
import static com.rhbgroup.dte.obc.common.func.Functions.peek;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.RandomGenerator;
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
import com.rhbgroup.dte.obc.model.AccountFilterCondition;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackRequest;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryRequest;
import com.rhbgroup.dte.obc.model.CDRBTransferType;
import com.rhbgroup.dte.obc.model.CreditDebitIndicator;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
  @Transactional(rollbackOn = RuntimeException.class)
  public InitTransactionResponse initTransaction(InitTransactionRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();

    AccountModel linkedAccount =
        accountService.getActiveAccountByUserIdAndBakongId(
            new AccountFilterCondition()
                .userId(currentUser.getUserId().toString())
                .bakongId(currentUser.getBakongId()));

    ConfigService transactionConfig =
        this.configService.loadJSONValue(ConfigConstants.Transaction.mapCurrency(request.getCcy()));

    // Validate transaction request
    TransactionValidator.validateInitTransaction(request, transactionConfig, linkedAccount);

    if (request.getType().equals(TransactionType.WALLET)) {
      try {
        PGProfileResponse userProfile =
            pgRestClient.getUserProfile(Collections.singletonList(request.getDestinationAcc()));

        // Validate destination account
        log.info("Bakong user profile >> {}", userProfile);
      } catch (BizException ex) {
        throw new BizException(ResponseMessage.TRANSACTION_TO_UNAVAILABLE_ACCOUNT);
      }
    }

    // Validate CASA account balance
    CDRBGetAccountDetailResponse casaAccount =
        cdrbRestClient.getAccountDetail(
            new CDRBGetAccountDetailRequest()
                .accountNo(request.getSourceAcc())
                .cifNo(currentUser.getCif()));
    AccountValidator.validateCurrentBalance(casaAccount, request.getAmount());

    // Getting fee and cashback
    CDRBFeeAndCashbackResponse feeAndCashback =
        cdrbRestClient.getFeeAndCashback(
            new CDRBFeeAndCashbackRequest()
                .amount(request.getAmount())
                .currencyCode(request.getCcy())
                .transactionType(AppConstants.Transaction.OBC_TOP_UP));

    TransactionModel transactionModel =
        new TransactionModel()
            .initRefNumber(RandomGenerator.getDefaultRandom().nextString())
            .userId(BigDecimal.valueOf(currentUser.getUserId()))
            .fromAccount(request.getSourceAcc())
            .toAccount(request.getDestinationAcc())
            .toAccountCurrency(request.getCcy())
            .creditDebitIndicator(CreditDebitIndicator.D)
            .trxCcy(request.getCcy())
            .payerName(linkedAccount.getAccountName())
            .transferMessage(request.getDesc())
            .transferType(request.getType())
            .trxAmount(request.getAmount())
            .trxFee(feeAndCashback.getFee())
            .trxCashback(feeAndCashback.getCashBack())
            .trxDate(OffsetDateTime.now())
            .trxStatus(TransactionStatus.PENDING);

    // Store PENDING transaction
    save(transactionModel);

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
                .initRefNumber(transactionModel.getInitRefNumber())
                .debitAmount(transactionModel.getTrxAmount())
                .debitCcy(transactionModel.getTrxCcy())
                .requireOtp(trxOtpEnabled)
                .fee(feeAndCashback.getFee()));
  }

  @Override
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
                        () -> new BizException(ResponseMessage.INTERNAL_SERVER_ERROR)))
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
    of(transactionMapper::toCDRBTransferRequest)
        .andThen(
            transferRequest -> {
              GetAccountDetailResponse accountDetail =
                  accountService.getAccountDetail(
                      new GetAccountDetailRequest().accNumber(transferRequest.getFromAccountNo()));

              transferRequest.setTransferType(CDRBTransferType.BAKONG_LINK_CASA_EWALLET);
              transferRequest.setCifNumber(currentUser.getCif());
              transferRequest.setObcUserId(BigDecimal.valueOf(currentUser.getUserId()));
              transferRequest.setToAccuntCurrency(transferRequest.getCurrencyCode());
              transferRequest.setTransactionCurrencyAmount(transferRequest.getAmount());
              transferRequest.setAccountCurrencyCode(accountDetail.getData().getAccCcy());

              return transferRequest;
            })
        .andThen(cdrbRestClient::transfer)
        .andThen(
            transferResponse ->
                new CDRBTransferInquiryRequest().correlationId(transferResponse.getCorrelationId()))
        .andThen(this::transactionInquiry)
        .andThen(
            inquiryResponse ->
                new FinishTransactionResponse()
                    .status(ResponseHandler.ok())
                    .data(
                        new FinishTransactionResponseAllOfData()
                            .transactionDate("")
                            .transactionId("trxId")
                            .transactionHash("hash")))
        .apply(transaction);

    return new FinishTransactionResponse().status(ResponseHandler.ok());
  }

  private Object transactionInquiry(CDRBTransferInquiryRequest request) {
    //     Object response = cdrbRestClient.transactionInquiry(request);
    //     if ("PENDING".equals(response.getStatus)) {
    //        transactionInquiry(request);
    //     } else {
    //        recordTransactionResult();
    //        return object;
    //     }
    //     return transactionInquiry
    return null;
  }
}
