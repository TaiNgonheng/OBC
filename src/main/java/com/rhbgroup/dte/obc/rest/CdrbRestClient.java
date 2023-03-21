package com.rhbgroup.dte.obc.rest;

import com.hazelcast.internal.cluster.impl.operations.OnJoinOp;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.model.PGAuthRequest;
import com.rhbgroup.dte.obc.model.PGAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CdrbRestClient {
    private final SpringRestUtil restUtil;
    @Value("${app.rest.cdrb-url}")
    String cdrbBaseUrl;

    public Object getHmsKey(Map<String, String> requestParams) {
        return restUtil.sendGet(
                cdrbBaseUrl,requestParams, null, ParameterizedTypeReference.forType(Object.class));
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
