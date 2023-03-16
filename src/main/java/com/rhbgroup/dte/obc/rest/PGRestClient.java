package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.model.PGAuthRequest;
import com.rhbgroup.dte.obc.model.PGAuthResponse;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PGRestClient {

  private final SpringRestUtil restUtil;

  @Value("${app.rest.pg1-url}")
  String pg1BaseUrl;

  public PGAuthResponse login(PGAuthRequest authRequest) {
    return restUtil.sendPost(
        pg1BaseUrl, authRequest, ParameterizedTypeReference.forType(PGAuthResponse.class));
  }

  public PGProfileResponse getUserProfile(Map<String, String> requestParams, String token) {
    Map<String, String> header = new HashMap<>();
    header.put("Authorization", "Bearer ".concat(token));

    return restUtil.sendGet(
        pg1BaseUrl,
        requestParams,
        header,
        ParameterizedTypeReference.forType(PGProfileResponse.class));
  }
}
