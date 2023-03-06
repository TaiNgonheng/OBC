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
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "otp_id")
  private String otpId;

  @Column(name = "mobile_no", nullable = false)
  private String mobileNo;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "cif_no")
  private String cifNo;

  @Column(name = "created_date")
  private Instant createdDate;

  @Column(name = "updated_date")
  private Instant updatedDate;
}
