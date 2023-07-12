package com.rhbgroup.dte.obc.domains.transaction.repository.entity;

import com.rhbgroup.dte.obc.model.CreditDebitIndicator;
import com.rhbgroup.dte.obc.model.TransactionStatus;
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
@Entity(name = "tbl_obc_transaction")
public class TransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "transfer_type", length = 20, nullable = false)
  private String transferType;

  @Column(name = "transfer_message", length = 250)
  private String transferMessage;

  @Column(name = "init_ref_number", length = 32, nullable = false, unique = true)
  private String initRefNumber;

  @Column(name = "trx_hash", length = 50, nullable = false, unique = true)
  private String trxHash;

  @Column(name = "trx_amount", nullable = false)
  private Double trxAmount;

  @Column(name = "trx_fee", nullable = false)
  private Double trxFee;

  @Column(name = "trx_cashback", nullable = false)
  private Double trxCashback;

  @Column(name = "trx_ccy", length = 3)
  private String trxCcy;

  @Column(name = "trx_date", insertable = false, updatable = false)
  private Instant trxDate;

  @Column(name = "trx_completion_date")
  private Instant trxCompletionDate;

  @Column(name = "trx_status", length = 10, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionStatus trxStatus;

  @Column(name = "from_account", length = 50, nullable = false)
  private String fromAccount;

  @Column(name = "payer_name", length = 30)
  private String payerName;

  @Column(name = "to_account", length = 50, nullable = false)
  private String toAccount;

  @Column(name = "to_account_currency", length = 5, nullable = false)
  private String toAccountCurrency;

  @Column(name = "recipient_bic", length = 30)
  private String recipientBIC;

  @Column(name = "recipient_name", length = 30)
  private String recipientName;

  @Column(name = "credit_debit_indicator", length = 5, nullable = false)
  @Enumerated(EnumType.STRING)
  private CreditDebitIndicator creditDebitIndicator;
}
