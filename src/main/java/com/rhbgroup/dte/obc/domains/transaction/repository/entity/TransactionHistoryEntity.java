package com.rhbgroup.dte.obc.domains.transaction.repository.entity;

import com.rhbgroup.dte.obc.model.CreditDebitIndicator;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "tbl_obc_sibs_transaction_history")
public class TransactionHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "channel_id", updatable = false, nullable = false)
  private String channelId;

  @Column(name = "transfer_type", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType transferType;

  @Column(name = "transfer_message", updatable = false)
  private String transferMessage;

  @Column(name = "trx_id", updatable = false, nullable = false, unique = true)
  private String trxId;

  @Column(name = "trx_amount_in_acct_currency", updatable = false, nullable = false)
  private Double amount;

  @Column(name = "trx_date", updatable = false, nullable = false)
  private Instant trxDate;

  @Column(name = "trx_hash", updatable = false, nullable = false, unique = true)
  private String trxHash;

  @Column(name = "trx_status", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionStatus trxStatus;

  @Column(name = "currency_code", updatable = false, nullable = false)
  private String currencyCode;

  @Column(name = "from_account", updatable = false, nullable = false)
  private String fromAccount;

  @Column(name = "to_account", updatable = false, nullable = false)
  private String toAccount;

  @Column(name = "credit_debit_indicator", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  private CreditDebitIndicator creditDebitIndicator;

  @Column(name = "trx_amnt", updatable = false, nullable = false)
  private Double tranAmnt;

  @Column(name = "tran_curr", updatable = false, nullable = false)
  private String tranCurr;

  @Column(name = "tran_fee_amnt", updatable = false, nullable = false)
  private Double tranFeeAmnt;

  @Column(name = "fee_amnt_in_acct_currency", updatable = false, nullable = false)
  private Double feeAmntInAcctCurrency;
}
