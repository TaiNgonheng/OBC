package com.rhbgroup.dte.obc.common.util;

import com.hazelcast.internal.util.MapUtil;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.exceptions.BizException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class SpringRestUtil {

  private final RestTemplate restTemplate;

  public <T> T sendGet(
      @NotBlank String url,
      Map<String, String> parameters,
      Map<String, String> headers,
      ParameterizedTypeReference<T> typeReference)
      throws RestClientException {

    if (parameters != null && !parameters.isEmpty()) {
      return sendRequest(
          buildUrlWithParams(url, parameters), HttpMethod.GET, headers, null, typeReference);
    }
    return sendRequest(url, HttpMethod.GET, headers, null, typeReference);
  }

  public <T> T sendGet(
      @NotBlank String url,
      Map<String, String> paths,
      Map<String, String> parameters,
      Map<String, String> headers,
      ParameterizedTypeReference<T> typeReference)
      throws RestClientException {

    return sendRequest(
        buildUrlWithPathsAndParams(url, paths, parameters),
        HttpMethod.GET,
        headers,
        null,
        typeReference);
  }

  public String withPathParams(String url, List<String> pathParams) {
    String paths = pathParams.stream().reduce((s1, s2) -> s1.concat("/").concat(s2)).orElse("");
    return url + paths;
  }

  private String buildUrlWithParams(String url, Map<String, String> parameters) {
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      uriBuilder.setCharset(StandardCharsets.UTF_8);
      for (Map.Entry<String, String> entrySet : parameters.entrySet()) {
        uriBuilder.addParameter(entrySet.getKey(), entrySet.getValue());
      }

      return uriBuilder.build().toString();
    } catch (URISyntaxException ex) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
  }

  private String buildUrlWithPathsAndParams(
      String url, Map<String, String> paths, Map<String, String> parameters) {
    try {
      if (!MapUtil.isNullOrEmpty(paths)) url = new UriTemplate(url).expand(paths).toString();
      URIBuilder uriBuilder = new URIBuilder(url);
      uriBuilder.setCharset(StandardCharsets.UTF_8);
      if (!MapUtil.isNullOrEmpty(parameters)) {
        for (Map.Entry<String, String> entrySet : parameters.entrySet()) {
          uriBuilder.addParameter(entrySet.getKey(), entrySet.getValue());
        }
      }
      return uriBuilder.build().toString();
    } catch (URISyntaxException ex) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
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
      ParameterizedTypeReference<T> typeReference) {
    return sendRequest(url, HttpMethod.POST, headers, body, typeReference);
  }

  public <T> T sendPost(
      String url,
      Map<String, String> paths,
      Map<String, String> parameters,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference<T> typeReference)
      throws RestClientException {
    return sendRequest(
        buildUrlWithPathsAndParams(url, paths, parameters),
        HttpMethod.POST,
        headers,
        body,
        typeReference);
  }

  private <T> T sendRequest(
      String url,
      HttpMethod method,
      Map<String, String> header,
      Object body,
      ParameterizedTypeReference<T> parameterizedTypeReference) {

    try {
      HttpEntity<Object> httpEntity = new HttpEntity<>(body, buildHeader(header));
      ResponseEntity<T> response =
          restTemplate.exchange(url, method, httpEntity, parameterizedTypeReference);

      log.info("System response >> {}", response.getBody());
      return response.getBody();

    } catch (RestClientException ex) {
      log.error("Internal API response with error >> {}", ex.getMessage());
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
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
