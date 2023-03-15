package com.rhbgroup.dte.obc.dto.response;

import lombok.Data;

@Data
public class BakongGetProfileResponse {
  private String accountName;
  private String accountId;
  private String name;
  private String bankName;
  private String email;
  private String kycStatus;
  private String accountStatus;
  private String phone;
  private boolean frozen;
}
