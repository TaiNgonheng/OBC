package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.PGAuthRequest;
import com.rhbgroup.dte.obc.model.PGAuthResponse;
import com.rhbgroup.dte.obc.model.PGAuthResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.PGResponseStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PGRestClient {

  private final SpringRestUtil restUtil;

  @Value("${obc.pg1.url}")
  protected String pg1BaseUrl;

  public PGAuthResponseAllOfData login(PGAuthRequest authRequest) {
    String loginUrl = "/api/authenticate";
    PGAuthResponse pgAuthResponse =
        restUtil.sendPost(
            pg1BaseUrl.concat(loginUrl),
            authRequest,
            ParameterizedTypeReference.forType(PGAuthResponse.class));

    verifyStatus(pgAuthResponse.getStatus());
    return pgAuthResponse.getData();
  }

  public PGProfileResponse getUserProfile(List<String> pathParams, String token) {
    String paths =
        restUtil.withPathParams("/tps/api/fst-iroha-accounts/find-by-account-name/", pathParams);
    Map<String, String> header = new HashMap<>();
    header.put("Authorization", "Bearer ".concat(token));

    PGProfileResponse responseObject =
        restUtil.sendGet(
            pg1BaseUrl.concat(paths),
            header,
            ParameterizedTypeReference.forType(PGProfileResponse.class));

    if (null == responseObject || responseObject.getAccountId() == null) {
      throw new BizException(ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS);
    }

    return responseObject;
  }

  private void verifyStatus(PGResponseStatus responseStatus) {
    if (null != responseStatus && AppConstants.STATUS.SUCCESS != responseStatus.getCode()) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
  }
}
