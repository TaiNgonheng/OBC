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

  @Column(name = "config_key", nullable = false)
  private String configKey;

  @Column(name = "config_value", nullable = false)
  private String configValue;

  @Column(name = "created_date")
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "updated_by")
  private String updatedBy;
}
