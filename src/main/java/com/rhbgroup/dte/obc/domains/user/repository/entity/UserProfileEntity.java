package com.rhbgroup.dte.obc.domains.user.repository.entity;

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
@Entity(name = "tbl_obc_profile")
public class UserProfileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", unique = true, nullable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "otp_verified_status")
  private Boolean otpVerifiedStatus;

  @Column(name = "otp_verified_date")
  private Instant otpVerifiedDate;

  @Column(name = "mobile_no", unique = true)
  private String mobileNo;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "cif_no")
  private String cifNo;

  @Column(name = "login_attempt")
  private Integer loginAttempt;

  @Column(name = "lock_time")
  private Instant lockTime;

  @Column(name = "deleted")
  private boolean deleted;

  @Column(name = "created_date", insertable = false, updatable = false)
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;

  @Column(name = "updated_by")
  private String updatedBy;
}
