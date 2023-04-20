package com.rhbgroup.dte.obc.domains.account.repository.entity;

import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.model.CasaAccountType;
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
@Entity(name = "tbl_obc_account")
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "account_id", unique = true)
  private String accountId;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "bakong_id", nullable = false)
  private String bakongId;

  @Column(name = "account_name")
  private String accountName;

  @Column(name = "account_type")
  @Enumerated(EnumType.STRING)
  private CasaAccountType accountType;

  @Column(name = "account_ccy")
  private String accountCcy;

  @Column(name = "account_status")
  private String accountStatus;

  @Column(name = "linked_status")
  @Enumerated(EnumType.STRING)
  private LinkedStatusEnum linkedStatus;

  @Column(name = "created_date", insertable = false, updatable = false)
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "updated_by")
  private String updatedBy;
}
