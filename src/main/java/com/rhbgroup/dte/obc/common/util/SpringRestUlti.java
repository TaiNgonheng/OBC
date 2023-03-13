package com.rhbgroup.dte.obc.common.util;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringRestUlti {
  @Autowired private SpringRestFactory springRestFactory;

  public <T> T sendGet(String url, ParameterizedTypeReference typeReference) {
    return springRestFactory.sendRequest(url, HttpMethod.GET, null, null, typeReference);
  }

  public <T> T sendGet(
      String url, Map<String, String> headers, ParameterizedTypeReference typeReference) {
    return springRestFactory.sendRequest(url, HttpMethod.GET, headers, null, typeReference);
  }

  public <T> T sendPost(String url, Object body, ParameterizedTypeReference typeReference) {
    return springRestFactory.sendRequest(url, HttpMethod.POST, null, body, typeReference);
  }

  public <T> T sendPost(
      String url,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference typeReference) {
    return springRestFactory.sendRequest(url, HttpMethod.POST, headers, body, typeReference);
  }
}
