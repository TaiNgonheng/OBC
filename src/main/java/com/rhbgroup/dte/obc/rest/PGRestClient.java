package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.dto.request.BakongLoginRequest;
import com.rhbgroup.dte.obc.dto.response.BakongGetProfileResponse;
import com.rhbgroup.dte.obc.dto.response.BakongLoginResponse;
import com.rhbgroup.dte.obc.exceptions.BizException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class PGRestClient {

  private SpringRestUtil restUtil;

  @Value("${app.rest.pg1-url}")
  private String pg1BaseUrl;

  public BakongLoginResponse login(BakongLoginRequest bakongLoginRequest) {
    try {
      return restUtil.sendPost(
          pg1BaseUrl,
          bakongLoginRequest,
          ParameterizedTypeReference.forType(BakongLoginResponse.class));

    } catch (RestClientException ex) {
      throw new BizException(ResponseMessage.PG1_COMMUNICATION_FAILURE);
    }
  }

  public BakongGetProfileResponse getUserProfile(Map<String, String> requestParams, String token) {
    try {
      Map<String, String> header = new HashMap<>();
      header.put("Authorization", "Bearer ".concat(token));

      return restUtil.sendGet(
              pg1BaseUrl,
              requestParams,
              header,
              ParameterizedTypeReference.forType(BakongGetProfileResponse.class));

    } catch (RestClientException ex) {
      throw new BizException(ResponseMessage.PG1_COMMUNICATION_FAILURE);
    }
  }
}
