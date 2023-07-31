package com.rhbgroup.dte.obc.domains.transaction.mapper;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.util.ObcDateUtils;
import com.rhbgroup.dte.obc.common.util.RandomGenerator;
import com.rhbgroup.dte.obc.domains.transaction.model.SIBSBatchTransaction;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionHistoryEntity;
import com.rhbgroup.dte.obc.model.BakongTransactionStatus;
import com.rhbgroup.dte.obc.model.CDRBFeeAndCashbackResponse;
import com.rhbgroup.dte.obc.model.CDRBTransactionHistoryResponseTransactions;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferRequest;
import com.rhbgroup.dte.obc.model.CDRBTransferType;
import com.rhbgroup.dte.obc.model.CasaAccountType;
import com.rhbgroup.dte.obc.model.CreditDebitIndicator;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.TransactionHistoryModel;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(source = "trxDate", target = "trxDate", qualifiedByName = "toInstant")
  @Mapping(
      source = "trxCompletionDate",
      target = "trxCompletionDate",
      qualifiedByName = "toInstant")
  TransactionEntity toEntity(TransactionModel model);

  @Named("toInstant")
  default Instant toInstant(OffsetDateTime offsetDateTime) {
    return null == offsetDateTime ? null : offsetDateTime.toInstant();
  }

  default TransactionModel toPendingTransactionModel(
      Long userId,
      InitTransactionRequest request,
      PGProfileResponse userProfile,
      String accountName,
      CDRBFeeAndCashbackResponse feeAndCashback) {

    return new TransactionModel()
        .initRefNumber(RandomGenerator.getDefaultRandom().nextString())
        .userId(BigDecimal.valueOf(userId))
        .fromAccount(request.getSourceAcc())
        .toAccount(request.getDestinationAcc())
        .toAccountCurrency(request.getCcy())
        .recipientName(userProfile.getAccountName())
        .creditDebitIndicator(CreditDebitIndicator.D)
        .trxCcy(request.getCcy())
        .payerName(accountName)
        .transferMessage(request.getDesc())
        .transferType(request.getType())
        .trxAmount(request.getAmount())
        .trxFee(feeAndCashback.getFee())
        .trxCashback(feeAndCashback.getCashBack())
        .trxDate(OffsetDateTime.now())
        .trxStatus(TransactionStatus.PENDING);
  }

  @Mapping(source = "trxAmount", target = "amount")
  @Mapping(source = "trxFee", target = "fees")
  @Mapping(source = "trxCashback", target = "cashBack")
  @Mapping(source = "trxCcy", target = "currencyCode")
  @Mapping(source = "fromAccount", target = "fromAccountNo")
  @Mapping(source = "toAccount", target = "toAccountNo")
  @Mapping(source = "toAccountCurrency", target = "toAccuntCurrency")
  @Mapping(target = "transferType", ignore = true)
  CDRBTransferRequest toCDRBTransferRequestBaseMapping(TransactionEntity entity);

  default CDRBTransferRequest toCDRBTransferRequestAdditionalMapping(
      CDRBTransferRequest originalRequest,
      CustomUserDetails userDetails,
      GetAccountDetailResponse accountDetailResponse) {

    // Mandatory additional fields
    originalRequest.setTransferType(CDRBTransferType.BAKONG_LINK_CASA_EWALLET);
    originalRequest.setCifNumber(userDetails.getCif());
    originalRequest.setObcUserId(BigDecimal.valueOf(userDetails.getUserId()));
    originalRequest.setAccountCurrencyCode(accountDetailResponse.getData().getAccCcy());
    originalRequest.setFromAccountType(
        CasaAccountType.fromValue(accountDetailResponse.getData().getAccType().getValue()));

    // Optional
    originalRequest.setTransactionCurrencyAmount(originalRequest.getAmount());
    originalRequest.setFeeTransactionCurrencyAmount(originalRequest.getFees());
    originalRequest.setCashBackTransactionCurrencyAmount(originalRequest.getCashBack());

    return originalRequest;
  }

  default FinishTransactionResponse toFinishTransactionResponse(
      CDRBTransferInquiryResponse inquiryResponse) {

    OffsetDateTime completionDate = inquiryResponse.getTransferCompletionDate();
    String extRef = inquiryResponse.getExternalSystemRef();

    return new FinishTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new FinishTransactionResponseAllOfData()
                .transactionDate(
                    null == completionDate
                        ? null
                        : BigDecimal.valueOf(completionDate.toInstant().toEpochMilli()))
                .transactionId(inquiryResponse.getCorrelationId())
                .transactionHash(null == extRef ? null : inquiryResponse.getExternalSystemRef()));
  }

  @Mapping(source = "fromAccount", target = "sourceAcc")
  @Mapping(source = "toAccount", target = "destinationAcc")
  @Mapping(source = "transferType", target = "type")
  @Mapping(source = "tranAmnt", target = "amount")
  @Mapping(source = "tranFeeAmnt", target = "fee")
  @Mapping(source = "entity", target = "debitAmount", qualifiedByName = "getTransactionDebitAmount")
  @Mapping(source = "tranCurr", target = "ccy")
  @Mapping(source = "transferMessage", target = "desc")
  @Mapping(source = "trxStatus", target = "status", qualifiedByName = "toBakongStatusEnum")
  @Mapping(source = "creditDebitIndicator", target = "cdtDbtInd")
  @Mapping(source = "trxId", target = "transactionId")
  @Mapping(source = "trxDate", target = "transactionDate", qualifiedByName = "getDateInMillis")
  @Mapping(source = "trxHash", target = "transactionHash")
  TransactionHistoryModel toTransactionHistoryModel(TransactionHistoryEntity entity);

  @Named("getTransactionDebitAmount")
  default Double getTransactionDebitAmount(TransactionHistoryEntity entity) {
    BigDecimal debitAmount =
        BigDecimal.valueOf(entity.getTranAmnt()).add(BigDecimal.valueOf(entity.getTranFeeAmnt()));
    return debitAmount.doubleValue();
  }

  @Named("getDateInMillis")
  default Long getDateInMillis(Instant instant) {
    return instant == null ? null : instant.toEpochMilli();
  }

  @Named("toBakongStatusEnum")
  default BakongTransactionStatus toBakongStatusEnum(TransactionStatus status) {
    switch (status) {
      case PENDING:
        return BakongTransactionStatus.PENDING;
      case FAILED:
        return BakongTransactionStatus.FAILED;
      case COMPLETED:
        return BakongTransactionStatus.SUCCESS;
    }

    return null;
  }

  @Mapping(source = "bakongHash", target = "trxHash")
  @Mapping(source = "remark", target = "transferMessage")
  @Mapping(source = "senderAccountNumber", target = "fromAccount")
  @Mapping(source = "receiverAccountNumber", target = "toAccount")
  @Mapping(source = "obcUserId", target = "channelId")
  @Mapping(source = "debitCreditCode", target = "creditDebitIndicator")
  @Mapping(source = "transferId", target = "trxId")
  @Mapping(source = "currency", target = "currencyCode")
  @Mapping(source = "feeAmnt", target = "feeAmntInAcctCurrency")
  @Mapping(
      source = "transactionHistory",
      target = "trxDate",
      qualifiedByName = "GetInstantFromCDRBTrxHistory")
  TransactionHistoryEntity toTransactionHistoryEntity(
      CDRBTransactionHistoryResponseTransactions transactionHistory);

  @Mapping(source = "transactionCode", target = "transferType", qualifiedByName = "GetTransferType")
  @Mapping(source = "remark", target = "transferMessage")
  @Mapping(
      source = "transaction",
      target = "trxDate",
      qualifiedByName = "GetTransactionHistoryInstant")
  @Mapping(source = "transactionHash", target = "trxHash")
  @Mapping(source = "transactionCurrency", target = "tranCurr")
  @Mapping(source = "senderAccount", target = "fromAccount")
  @Mapping(source = "receiverAccount", target = "toAccount")
  @Mapping(target = "trxStatus", constant = "COMPLETED")
  @Mapping(source = "transaction", target = "trxId", qualifiedByName = "GetTransactionId")
  TransactionHistoryEntity toTransactionHistory(SIBSBatchTransaction transaction);

  List<TransactionHistoryEntity> toTransactionHistories(List<SIBSBatchTransaction> transactions);

  @Named("DateFromDDMMYYYY")
  default LocalDate getDateFromDDMMYYYY(String date) {
    if (date.length() == 7) date = "0" + date;
    return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  @Named("InstantFromDDMMYYYY")
  default Instant getInstantFromDDMMYYY(String date) {
    return getDateFromDDMMYYYY(date).atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  @Named("InstantFromLocalDate")
  default Instant getInstantFromLocalDate(LocalDate date) {
    return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  @Named("InstantFromStringYYYYMMDD")
  default Instant getInstantFromLocalDate(String dateStr) {
    return ObcDateUtils.toInstant(dateStr, ObcDateUtils.YYYY_MM_DD);
  }

  @Named("GetTransactionHistoryInstant")
  default Instant getTransactionHistoryInstant(SIBSBatchTransaction transaction) {
    if (!ObjectUtils.isEmpty(transaction)) {
      return getInstantFromStringDateTime(
          transaction.getTransactionDate(), transaction.getTransactionTime());
    }
    return null;
  }

  @Named("GetInstantFromCDRBTrxHistory")
  default Instant getInstantFromCDRBTrxHistory(
      CDRBTransactionHistoryResponseTransactions cdrbTrxHistory) {
    if (!ObjectUtils.isEmpty(cdrbTrxHistory)) {
      return getInstantFromStringDateTime(
          cdrbTrxHistory.getTransactionDate(), String.valueOf(cdrbTrxHistory.getTransactionTime()));
    }
    return null;
  }

  default Instant getInstantFromStringDateTime(String date, String time) {
    LocalDate d = getDateFromDDMMYYYY(date);
    if (time.length() == 5) time = "0" + time;
    LocalTime t = LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmmss"));
    return LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant();
  }

  @Named("GetTransactionId")
  default String getTransferId(SIBSBatchTransaction transaction) {
    if (!ObjectUtils.isEmpty(transaction)) {
      return transaction.getChannelId()
          + transaction.getJournalSequence()
          + getTransactionHistoryInstant(transaction).toEpochMilli();
    }
    return null;
  }

  @Named("GetTransferType")
  default TransactionType getTransactionType(String transactionCode) {
    if (StringUtils.isNotBlank(transactionCode)
        && AppConstants.Transaction.CASA_TO_WALLET_CODES.contains(transactionCode)) {
      return TransactionType.WALLET;
    }
    return null;
  }
}
