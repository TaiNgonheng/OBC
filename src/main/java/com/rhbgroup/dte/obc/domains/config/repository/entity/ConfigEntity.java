package com.rhbgroup.dte.obc.domains.config.repository.entity;

import java.time.Instant;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "tbl_obc_config")
public class ConfigEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @Column(name = "service_name", nullable = false)
  private String serviceName;

  @Column(name = "login", nullable = false)
  private String login;

  @Column(name = "secret", nullable = false)
  private String secret;

  @Column(name = "token", nullable = false)
  private String token;

  @Column(name = "required_trx_otp", nullable = false)
  private boolean requiredTrxOtp;

  @Column(name = "transaction_config")
  private String transactionConfig;

  @Column(name = "created_date")
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "updated_by")
  private String updatedBy;
}
