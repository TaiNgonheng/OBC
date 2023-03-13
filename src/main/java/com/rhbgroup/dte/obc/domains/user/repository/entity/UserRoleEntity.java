package com.rhbgroup.dte.obc.domains.user.repository.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "tbl_obc_user_role")
@Getter
@Setter
public class UserRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "user_id", unique = true, nullable = false)
  private String userId;

  @Column(name = "role", nullable = false)
  private String role;

  @Column(name = "permissions")
  private String permissions;

  @Column(name = "created_date", insertable = false)
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "updated_by")
  private String updatedBy;
}
