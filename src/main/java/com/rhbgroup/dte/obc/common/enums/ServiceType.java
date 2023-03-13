package com.rhbgroup.dte.obc.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceType {
  PG1("pg1", "", "", "", ""),
  INFO_BIP("infoBip", "", "", "", "");

  private String name;

  private String code;

  private String secret;

  private String token;

  private String url;

  ServiceType(String name, String code, String secret, String token, String url) {
    this.name = name;
    this.code = code;
    this.secret = secret;
    this.token = token;
    this.url = url;
  }

  @JsonValue
  public String getName() {
    return this.name;
  }

  @JsonValue
  public String getCode() {
    return this.code;
  }

  @JsonValue
  public String getSecret() {
    return this.secret;
  }

  @JsonValue
  public String getToken() {
    return this.token;
  }

  @JsonValue
  public String getUrl() {
    return this.url;
  }

  @JsonCreator
  public static ServiceType fromName(String name) {
    for (ServiceType serviceType : ServiceType.values()) {
      if (serviceType.getName().equals(name)) {
        return serviceType;
      }
    }
    throw new IllegalArgumentException("Unexpected name '" + name + "'");
  }
}
