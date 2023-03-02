package com.rhbgroup.dte.obc.domains.account.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;

@Getter
@Setter
@Entity(name = "tbl_account")
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "bakong_id", nullable = false)
  private Long bakongId;

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @Column(name = "active")
  private Boolean active;

  @Column(name = "created_date")
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;

}
