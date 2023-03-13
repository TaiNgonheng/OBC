package com.rhbgroup.dte.obc.dto.request;

import lombok.Data;

@Data
public class BakongLoginRequest {
  private String username;
  private String password;

  public BakongLoginRequest() {}

  public BakongLoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
