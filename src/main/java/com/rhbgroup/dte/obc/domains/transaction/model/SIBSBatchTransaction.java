package com.rhbgroup.dte.obc.domains.transaction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({
  "Record Type",
  "Journal Seq",
  "User ID",
  "Trx Date",
  "Trx Code",
  "AccountNo",
  "Sender Account",
  "Receiver Account",
  "Amount",
  "Trx Currency",
  "Remark",
  "Debit Credit Indicator",
  "Trx Hash",
  "Payment Ref No",
  "Bakong Status"
})
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SIBSBatchTransaction {

  @JsonProperty("Record Type")
  private String recordType;

  @JsonProperty("Journal Seq")
  private String journalSequence;

  @JsonProperty("User ID")
  private String userId;

  @JsonProperty("Trx Date")
  private String transactionDate;

  @JsonProperty("Trx Code")
  private String transactionCode;

  @JsonProperty("AccountNo")
  private String accountNumber;

  @JsonProperty("Sender Account")
  private String senderAccount;

  @JsonProperty("Receiver Account")
  private String receiverAccount;

  @JsonProperty("Amount")
  private BigDecimal amount;

  @JsonProperty("Trx Currency")
  private String transactionCurrency;

  @JsonProperty("Remark")
  private String remark;

  @JsonProperty("Debit Credit Indicator")
  private String debitCreditIndicator;

  @JsonProperty("Trx Hash")
  private String transactionHash;

  @JsonProperty("Payment Ref No")
  private String paymentReferenceNumber;

  @JsonProperty("Bakong Status")
  private String bakongStatus;
}
