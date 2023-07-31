package com.rhbgroup.dte.obc.domains.transaction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.rhbgroup.dte.obc.model.CreditDebitIndicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({
  "Record Type",
  "Journal Seq",
  "Channel ID",
  "Trx Date",
  "Trx Time",
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
  "Bakong Status",
  "Transaction Equivalent Amount",
  "Transaction Equivalent Currency",
  "Transaction Charges Amount",
  "Debit Charges in account currency"
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

  @JsonProperty("Channel ID")
  private String channelId;

  @JsonProperty("Trx Date")
  private String transactionDate;

  @JsonProperty("Trx Time")
  private String transactionTime;

  @JsonProperty("Trx Code")
  private String transactionCode;

  @JsonProperty("AccountNo")
  private String accountNumber;

  @JsonProperty("Sender Account")
  private String senderAccount;

  @JsonProperty("Receiver Account")
  private String receiverAccount;

  @JsonProperty("Amount")
  private Double amount;

  @JsonProperty("Trx Currency")
  private String transactionCurrency;

  @JsonProperty("Remark")
  private String remark;

  @JsonProperty("Debit Credit Indicator")
  private CreditDebitIndicator creditDebitIndicator;

  @JsonProperty("Trx Hash")
  private String transactionHash;

  @JsonProperty("Payment Ref No")
  private String paymentReferenceNumber;

  @JsonProperty("Bakong Status")
  private String bakongStatus;

  @JsonProperty("Transaction Equivalent Amount")
  private Double tranAmnt;

  @JsonProperty("Transaction Equivalent Currency")
  private String tranCurr;

  @JsonProperty("Transaction Charges Amount")
  private Double tranFeeAmnt;

  @JsonProperty("Debit Charges in account currency")
  private Double feeAmntInAcctCurrency;
}
