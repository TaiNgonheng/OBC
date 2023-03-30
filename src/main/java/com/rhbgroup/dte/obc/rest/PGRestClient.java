package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.ObcStringUtils;
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
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public class PGRestClient {

  private static final String GET_USER_PROFILE_URL =
      "/tps/api/fst-iroha-accounts/find-by-account-name";

  private final SpringRestUtil restUtil;
  private final CacheUtil cacheUtil;

  @Value("${obc.pg1.url}")
  protected String baseUrl;

  @Value("${obc.pg1.username}")
  protected String username;

  @Value("${obc.pg1.password}")
  protected String password;

  @PostConstruct
  public void initCache() {
    cacheUtil.createCache(CacheConstants.PGCache.CACHE_NAME, Duration.ONE_MINUTE);
  }

  public PGProfileResponse getUserProfile(List<String> pathParams) {

    String paths = restUtil.withPathParams(GET_USER_PROFILE_URL.concat("/"), pathParams);
    String bakongId = CollectionUtils.isEmpty(pathParams) ? null : pathParams.get(0);

    Map<String, String> header = new HashMap<>();
    header.put("Authorization", "Bearer ".concat(getAccessToken(bakongId)));

    PGProfileResponse responseObject =
        restUtil.sendGet(
            baseUrl.concat(paths),
            header,
            ParameterizedTypeReference.forType(PGProfileResponse.class));

    if (null == responseObject || responseObject.getAccountId() == null) {
      throw new BizException(ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS);
    }

    return responseObject;
  }

  private String getAccessToken(String bakongId) {

    String pgLoginKey =
        StringUtils.isNotBlank(bakongId) ? bakongId : ObcStringUtils.randomString(10);

    String tokenFromCache =
        cacheUtil.getValueFromKey(CacheConstants.PGCache.CACHE_NAME, pgLoginKey);
    if (tokenFromCache != null) {
      return tokenFromCache;
    }

    String pg1AccessToken = login().getIdToken();
    cacheUtil.addKey(CacheConstants.PGCache.CACHE_NAME, pgLoginKey, pg1AccessToken);

    return pg1AccessToken;
  }

  private PGAuthResponseAllOfData login() {
    String loginUrl = "/api/authenticate";
    PGAuthResponse pgAuthResponse =
        restUtil.sendPost(
            baseUrl.concat(loginUrl),
            new PGAuthRequest().username(username).password(password),
            ParameterizedTypeReference.forType(PGAuthResponse.class));

    verifyStatus(pgAuthResponse.getStatus());
    return pgAuthResponse.getData();
  }

  private void verifyStatus(PGResponseStatus responseStatus) {
    if (null != responseStatus && AppConstants.STATUS.SUCCESS != responseStatus.getCode()) {
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
  }
}
