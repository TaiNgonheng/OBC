package com.rhbgroup.dte.obc.domains.transaction.service.impl;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.common.util.RandomGenerator;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapper;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapperImpl;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionRepository;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionValidator;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackRequest;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.InitTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.math.BigDecimal;
import java.time.Instant;
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

  private final JwtTokenUtils jwtTokenUtils;

  private final UserProfileService userProfileService;
  private final UserAuthService userAuthService;
  private final ConfigService configService;

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  private final PGRestClient pgRestClient;
  private final CDRBRestClient cdrbRestClient;

  private final TransactionMapper transactionMapper = new TransactionMapperImpl();

  @Override
  public void save(TransactionModel transactionModel) {
    Functions.of(transactionMapper::toEntity)
        .andThen(transactionRepository::save)
        .apply(transactionModel);
  }

  @Override
  @Transactional(rollbackOn = RuntimeException.class)
  public InitTransactionResponse initTransaction(InitTransactionRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    AccountEntity linkedAccount =
        accountRepository
            .findByUserIdAndBakongIdAndLinkedStatus(
                currentUser.getUserId(), currentUser.getBakongId(), LinkedStatusEnum.COMPLETED)
            .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    ConfigService transactionConfig =
        this.configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY);

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
            .creditDebitIndicator("D")
            .trxCcy(request.getCcy())
            .payerName(linkedAccount.getAccountName())
            .recipientBIC("BIC")
            .recipientName("Name")
            .transferMessage("Transfer message on " + Instant.now().toString())
            .transferType(request.getType())
            .trxAmount(request.getAmount())
            .trxDate(OffsetDateTime.now())
            .trxStatus(TransactionStatus.PENDING);

    // Store PENDING transaction
    save(transactionModel);

    // Get otp required config
    Integer trxOtpEnabled =
        transactionConfig.getValue(ConfigConstants.Transaction.OTP_REQUIRED, Integer.class);

    return new InitTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new InitTransactionResponseAllOfData()
                .initRefNumber(transactionModel.getInitRefNumber())
                .debitAmount(transactionModel.getTrxAmount())
                .debitCcy(transactionModel.getTrxCcy())
                .requireOtp(1 == trxOtpEnabled)
                .fee(feeAndCashback.getFee()));
  }

  @Override
  public FinishTransactionResponse finishTransaction(
      String authorization, FinishTransactionRequest finishTransactionRequest) {
    Long userId = Long.parseLong(jwtTokenUtils.getUserId(authorization));
    UserModel userProfile = userProfileService.findByUserId(userId);
    if (finishTransactionRequest.getKey().equals(userProfile.getPassword())) {
      throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
    }
    return null;
  }
}
