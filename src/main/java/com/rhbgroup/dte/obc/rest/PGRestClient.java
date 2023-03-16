package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.PGAuthRequest;
import com.rhbgroup.dte.obc.model.PGAuthResponse;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class PGRestClient {

  private final SpringRestUtil restUtil;

  @Value("${app.rest.pg1-url}")
  private String pg1BaseUrl;

  public PGAuthResponse login(PGAuthRequest authRequest) {
    try {
      return restUtil.sendPost(
          pg1BaseUrl, authRequest, ParameterizedTypeReference.forType(PGAuthResponse.class));

    } catch (RestClientException ex) {
      throw new BizException(ResponseMessage.PG1_COMMUNICATION_FAILURE);
    }
  }

  public PGProfileResponse getUserProfile(Map<String, String> requestParams, String token) {
    try {
      Map<String, String> header = new HashMap<>();
      header.put("Authorization", "Bearer ".concat(token));

      return restUtil.sendGet(
          pg1BaseUrl,
          requestParams,
          header,
          ParameterizedTypeReference.forType(PGProfileResponse.class));

    } catch (RestClientException ex) {
      throw new BizException(ResponseMessage.PG1_COMMUNICATION_FAILURE);
    }
  }
}
