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

  @Column(name = "user_id", updatable = false, nullable = false)
  private Long userId;

  @Column(name = "transfer_type", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType transferType;

  @Column(name = "transfer_message", updatable = false)
  private String transferMessage;

  @Column(name = "trx_id", updatable = false, nullable = false, unique = true)
  private String trxId;

  @Column(name = "trx_amount", updatable = false, nullable = false)
  private Double trxAmount;

  @Column(name = "trx_date", updatable = false, nullable = false)
  private Instant trxDate;

  @Column(name = "trx_hash", updatable = false, nullable = false, unique = true)
  private String trxHash;

  @Column(name = "trx_status", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionStatus trxStatus;

  @Column(name = "trx_ccy", updatable = false, nullable = false)
  private String trxCcy;

  @Column(name = "from_account", updatable = false, nullable = false)
  private String fromAccount;

  @Column(name = "to_account", updatable = false, nullable = false)
  private String toAccount;

  @Column(name = "credit_debit_indicator", updatable = false, nullable = false)
  @Enumerated(EnumType.STRING)
  private CreditDebitIndicator creditDebitIndicator;
}
