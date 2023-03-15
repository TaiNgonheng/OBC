package com.rhbgroup.dte.obc.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class SpringRestUtil {
  @Resource private RestTemplate restTemplate;

  public <T> T sendGet(String url, Map<String, String> parameters, Map<String, String> headers, ParameterizedTypeReference<T> typeReference)
      throws RestClientException {

    if (parameters != null && !parameters.isEmpty()) {
      return sendRequest(buildUrlWithParams(url, parameters), HttpMethod.GET, headers, null, typeReference);
    }
    return sendRequest(url, HttpMethod.GET, headers, null, typeReference);
  }

  private String buildUrlWithParams(String url, Map<String, String> parameters) {

    if (StringUtils.isEmpty(url)) {
      return null;
    }
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      uriBuilder.setCharset(StandardCharsets.UTF_8);

      for (Map.Entry<String, String> entrySet : parameters.entrySet()) {
        uriBuilder.addParameter(entrySet.getKey(), entrySet.getValue());
      }

      return uriBuilder.build().toString();

    } catch (URISyntaxException ex) {
      return null;
    }
  }

  public <T> T sendGet(
      String url, Map<String, String> headers, ParameterizedTypeReference<T> typeReference)
      throws RestClientException {
    return sendRequest(url, HttpMethod.GET, headers, null, typeReference);
  }

  public <T> T sendPost(String url, Object body, ParameterizedTypeReference<T> typeReference)
      throws RestClientException {
    return sendRequest(url, HttpMethod.POST, null, body, typeReference);
  }

  public <T> T sendPost(
      String url,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference<T> typeReference)
      throws RestClientException {
    return sendRequest(url, HttpMethod.POST, headers, body, typeReference);
  }

  private <T> T sendRequest(
      String url,
      HttpMethod method,
      Map<String, String> header,
      Object body,
      ParameterizedTypeReference<T> parameterizedTypeReference) {
    HttpEntity<Object> httpEntity = new HttpEntity<>(body, buildHeader(header));
    ResponseEntity<T> response =
        restTemplate.exchange(url, method, httpEntity, parameterizedTypeReference);
    return response.getBody();
  }

  private HttpHeaders buildHeader(Map<String, String> headersMap) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    if (headersMap != null) {
      for (Map.Entry<String, String> entry : headersMap.entrySet()) {
        headers.set(entry.getKey(), entry.getValue());
      }
    }
    return headers;
  }
}
