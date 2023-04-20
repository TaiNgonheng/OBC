package com.rhbgroup.dte.obc.domains.transaction.repository.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_obc_temp_transaction_history")
public class TemporaryTransaction {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "transfer_type")
  private String transferType;

  @Column(name = "transfer_message")
  private String transferMessage;

  @Column(name = "trx_id")
  private String transactionId;

  @Column(name = "trx_amount")
  private Double transactionAmount;

  @Column(name = "trx_date")
  private LocalDate transactionDate;

  @Column(name = "trx_completion_date")
  private LocalDateTime transactionCompletionDate;

  @Column(name = "trx_hash")
  private String transactionHash;

  @Column(name = "trx_status")
  private String transactionStatus;

  @Column(name = "trx_ccy")
  private String transactionCCY;

  @Column(name = "from_account")
  private String fromAccount;

  @Column(name = "to_account")
  private String toAccount;

  @Column(name = "credit_debit_indicator")
  private String creditDebitIndicator;
}
