package com.rhbgroup.dte.obc.domains.transactions.repository.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_obc_sibs_transaction_history")
public class SIBSTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "transfer_message")
  private String transferMessage;

  @Column(name = "trx_amount")
  private BigDecimal amount;

  @Column(name = "trx_date")
  private LocalDate transactionDate;

  @Column(name = "trx_hash")
  private String transactionHash;

  @Column(name = "trx_ccy")
  private String transactionCurrency;

  @Column(name = "credit_debit_indicator")
  private String creditDebitIndicator;

  @Column(name = "record_type")
  private String recordType;

  @Column(name = "journal_seq")
  private String journalSequence;

  @Column(name = "trx_code")
  private String transactionCode;

  @Column(name = "account_number")
  private String accountNumber;

  @Column(name = "sender_account")
  private String senderAccount;

  @Column(name = "receiver_account")
  private String receiverAccount;

  @Column(name = "bakong_ref_no")
  private String bakongReferenceNumber;

  @Column(name = "bakong_status")
  private String bakongStatus;
}
