package com.rhbgroup.dte.obc.common.util;

import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
@Slf4j
public class SpringRestUlti {
  @Resource
  private RestTemplate restTemplate;

  public <T> T sendGet(String url, ParameterizedTypeReference typeReference) {
    return sendRequest(url, HttpMethod.GET, null, null, typeReference);
  }

  public <T> T sendGet(
      String url, Map<String, String> headers, ParameterizedTypeReference typeReference) {
    return sendRequest(url, HttpMethod.GET, headers, null, typeReference);
  }

  public <T> T sendPost(String url, Object body, ParameterizedTypeReference typeReference) {
    return sendRequest(url, HttpMethod.POST, null, body, typeReference);
  }

  public <T> T sendPost(
      String url,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference typeReference) {
    return sendRequest(url, HttpMethod.POST, headers, body, typeReference);
  }

  public <T> T sendRequest(
          String url,
          HttpMethod method,
          Map<String, String> header,
          Object body,
          ParameterizedTypeReference parameterizedTypeReference) {
    HttpEntity<Object> httpEntity = new HttpEntity<>(body, buildHeader(header));
    try {
      ResponseEntity<T> response = restTemplate.exchange(url, method, httpEntity, parameterizedTypeReference);
      return response.getBody();
    } catch (Exception e) {
      log.error("Http client request error: {}", e);
    }
    return null;
  }

  private HttpHeaders buildHeader(Map<String, String> headersMap) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    if (headersMap != null) {
      for (Map.Entry entry : headersMap.entrySet()) {
        headers.set(entry.getKey().toString(), entry.getValue().toString());
      }
    }
    return headers;
  }
}
