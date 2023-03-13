package com.rhbgroup.dte.obc.common.util;

import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SpringRestFactory {

  @Autowired RestTemplate restTemplate;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  public HttpHeaders buildHeader(Map<String, String> headersMap) {
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

  public <T> T sendRequest(
      String url,
      HttpMethod method,
      Map<String, String> header,
      Object body,
      ParameterizedTypeReference parameterizedTypeReference) {
    HttpEntity<Object> httpEntity = new HttpEntity<>(body, buildHeader(header));
    try {
      ResponseEntity<T> response =
          restTemplate.exchange(url, method, httpEntity, parameterizedTypeReference);
      return response.getBody();
    } catch (Exception e) {
      log.error("Http client request error: {}", e);
    }
    return null;
  }
}
