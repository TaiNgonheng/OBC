package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.model.CdrbGetHmsKeyResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class CdrbRestClient {
  private final SpringRestUtil restUtil;
  private final ConfigService configService;
  private String cdrbBaseUrl;
  @PostConstruct
  private void loadConfiguration() {
    cdrbBaseUrl =
            configService.getByConfigKey(
                    ConfigConstants.CDRB.CDRB_URL_KEY, ConfigConstants.VALUE, String.class);
  }

  public String getHmsKey() {
    Map<String, String> header = new HashMap<>();
    String paths =
            restUtil.withPathParams(ConfigConstants.CDRB.CDRB_GET_HSM_KEY_PATH, new ArrayList<>());
    header.put(ConfigConstants.CDRB.CDRB_API_KEY, ConfigConstants.CDRB.CDRB_API_KEY_VALUE);
    CdrbGetHmsKeyResponse cdrbGetHmsKeyResponse =
        restUtil.sendGet(
            cdrbBaseUrl.concat(paths),
            null,
            header,
            ParameterizedTypeReference.forType(CdrbGetHmsKeyResponse.class));
    return cdrbGetHmsKeyResponse.getZpk();
  }

    public Object login(Object authRequest) {
        return restUtil.sendPost(
                cdrbBaseUrl, authRequest, ParameterizedTypeReference.forType(Object.class));
    }

    public Object getAccountDetail(Map<String, String> requestParams) {
        return restUtil.sendGet(
                cdrbBaseUrl,requestParams, null, ParameterizedTypeReference.forType(Object.class));
    }
}
