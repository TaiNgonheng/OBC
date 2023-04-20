package com.rhbgroup.dte.obc.domains.transaction.mapper;

import com.rhbgroup.dte.obc.domains.transaction.model.SIBSBatchTransaction;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.entity.SIBSTransaction;
import com.rhbgroup.dte.obc.model.TransactionModel;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

  @Mapping(source = "paymentReferenceNumber", target = "bakongReferenceNumber")
  @Mapping(source = "remark", target = "transferMessage")
  @Mapping(
      source = "transactionDate",
      target = "transactionDate",
      qualifiedByName = "DateFromDDMMYYYY")
  @Mapping(source = "debitCreditIndicator", target = "creditDebitIndicator")
  SIBSTransaction toSIBSTransaction(SIBSBatchTransaction transaction);

  List<SIBSTransaction> toSIBSTransactions(List<SIBSBatchTransaction> transactions);

  @Named("DateFromDDMMYYYY")
  default LocalDate getDateFromDDMMYYYY(String date) {
    if (date.length() == 7) date = "0" + date;
    return LocalDate.parse(date, DateTimeFormatter.ofPattern("ddMMyyyy"));
  }
}
