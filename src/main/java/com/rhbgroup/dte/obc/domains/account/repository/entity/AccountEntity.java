package com.rhbgroup.dte.obc.domains.account.repository.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "bakong_id", nullable = false)
  private String bakongId;

  @Column(name = "account_id", nullable = false)
  private String accountId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "active")
  private Boolean active;

  @Column(name = "created_date")
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;
}
