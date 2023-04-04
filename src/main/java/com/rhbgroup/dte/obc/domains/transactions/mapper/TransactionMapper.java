package com.rhbgroup.dte.obc.domains.transactions.mapper;

import com.rhbgroup.dte.obc.domains.transactions.model.SIBSBatchTransaction;
import com.rhbgroup.dte.obc.domains.transactions.repository.entity.SIBSTransaction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

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
