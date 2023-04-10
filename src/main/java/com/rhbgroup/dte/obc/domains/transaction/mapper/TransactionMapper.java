package com.rhbgroup.dte.obc.domains.transaction.mapper;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionEntity;
import com.rhbgroup.dte.obc.model.CDRBTransferInquiryResponse;
import com.rhbgroup.dte.obc.model.CDRBTransferRequest;
import com.rhbgroup.dte.obc.model.CDRBTransferType;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.FinishTransactionResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(source = "trxDate", target = "trxDate", qualifiedByName = "toOffsetDateTime")
  @Mapping(
      source = "trxCompletionDate",
      target = "trxCompletionDate",
      qualifiedByName = "toOffsetDateTime")
  TransactionModel toModel(TransactionEntity entity);

  @Mapping(source = "trxDate", target = "trxDate", qualifiedByName = "toInstant")
  @Mapping(
      source = "trxCompletionDate",
      target = "trxCompletionDate",
      qualifiedByName = "toInstant")
  TransactionEntity toEntity(TransactionModel model);

  @Named("toOffsetDateTime")
  default OffsetDateTime toOffsetDateTime(Instant instant) {
    return null == instant ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

  @Named("toInstant")
  default Instant toInstant(OffsetDateTime offsetDateTime) {
    return null == offsetDateTime ? null : offsetDateTime.toInstant();
  }

  @Mapping(source = "trxAmount", target = "amount")
  @Mapping(source = "trxFee", target = "fees")
  @Mapping(source = "trxCashback", target = "cashBack")
  @Mapping(source = "trxCcy", target = "currencyCode")
  @Mapping(source = "fromAccount", target = "fromAccountNo")
  @Mapping(source = "toAccount", target = "toAccountNo")
  @Mapping(target = "transferType", ignore = true)
  CDRBTransferRequest toCDRBTransferRequestBaseMapping(TransactionEntity entity);

  default CDRBTransferRequest toCDRBTransferRequestAdditionalMapping(
      CDRBTransferRequest originalRequest,
      CustomUserDetails userDetails,
      GetAccountDetailResponse accountDetailResponse) {

    originalRequest.setTransferType(CDRBTransferType.BAKONG_LINK_CASA_EWALLET);
    originalRequest.setCifNumber(userDetails.getCif());
    originalRequest.setObcUserId(BigDecimal.valueOf(userDetails.getUserId()));

    // Need to be reviewed
    originalRequest.setToAccuntCurrency(originalRequest.getCurrencyCode());
    originalRequest.setTransactionCurrencyAmount(originalRequest.getAmount());

    originalRequest.setAccountCurrencyCode(accountDetailResponse.getData().getAccCcy());

    return originalRequest;
  }

  default FinishTransactionResponse toFinishTransactionResponse(
      CDRBTransferInquiryResponse inquiryResponse) {

    OffsetDateTime completionDate = inquiryResponse.getTransferCompletionDate();
    String extRef = inquiryResponse.getExternalSytemRef();

    return new FinishTransactionResponse()
        .status(ResponseHandler.ok())
        .data(
            new FinishTransactionResponseAllOfData()
                .transactionDate(null == completionDate ? null : completionDate.toString())
                .transactionId(inquiryResponse.getCorrelationId())
                .transactionHash(null == extRef ? null : inquiryResponse.getExternalSytemRef()));
  }
}
