package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.ObcDateUtils;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.InfoBipLoginResponse;
import com.rhbgroup.dte.obc.model.InfoBipSendOtpRequest;
import com.rhbgroup.dte.obc.model.InfoBipSendOtpRequestPlaceholders;
import com.rhbgroup.dte.obc.model.InfoBipSendOtpResponse;
import com.rhbgroup.dte.obc.model.InfoBipVerifyOtpRequest;
import com.rhbgroup.dte.obc.model.InfoBipVerifyOtpResponse;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class InfoBipRestClient {

  private static final String INFO_BIP_SEND_OTP_URL = "/2fa/2/pin";
  private static final String INFO_BIP_VERIFY_OTP_API_URL = "/2fa/2/pin/{pinId}/verify";
  private static final String INFO_BIP_LOGIN_API_URL = "/auth/1/oauth2/token";

  private static final String INFO_BIP_DATE_FORMAT = "hh:mm dd/MM/yy";

  private final SpringRestUtil restUtil;
  private final CacheUtil cacheUtil;
  private final JwtTokenUtils jwtTokenUtils;

  private static final Duration EXPIRE_TIME_TWO_MINUTES = new Duration(TimeUnit.MINUTES, 2);

  @Value("${obc.infobip.username}")
  protected String username;

  @Value("${obc.infobip.password}")
  protected String password;

  @Value("${obc.infobip.url}")
  protected String baseUrl;

  @Value("${obc.infobip.appId}")
  protected String appId;

  @Value("${obc.infobip.messageId}")
  protected String messageId;

  @Value("${obc.infobip.ncNeeded}")
  protected boolean ncNeeded;

  @PostConstruct
  private void initCache() {
    cacheUtil.createCache(CacheConstants.InfoBipCache.CACHE_NAME, EXPIRE_TIME_TWO_MINUTES);
  }

  public InfoBipSendOtpResponse sendOtp(String phone, String userId) {

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer ".concat(getAccessToken()));

    InfoBipSendOtpResponse sendSmsOtpResponse =
        restUtil.sendPost(
            baseUrl.concat(
                ncNeeded ? INFO_BIP_SEND_OTP_URL : INFO_BIP_SEND_OTP_URL.concat("?ncNeeded=false")),
            headers,
            new InfoBipSendOtpRequest()
                .applicationId(appId)
                .messageId(messageId)
                .to(phone)
                .from(ConfigConstants.InfoBip.INFO_BIP_SENDER_NAME)
                .placeholders(
                    new InfoBipSendOtpRequestPlaceholders()
                        .timestamp(ObcDateUtils.toDateString(new Date(), INFO_BIP_DATE_FORMAT))),
            ParameterizedTypeReference.forType(InfoBipSendOtpResponse.class));

    cacheUtil.addKey(
        CacheConstants.InfoBipCache.CACHE_NAME,
        CacheConstants.InfoBipCache.PIN_ID_KEY.concat(userId),
        sendSmsOtpResponse.getPinId());

    return sendSmsOtpResponse;
  }

  public Boolean verifyOtp(String otp, String userId) {

    String pinId =
        cacheUtil.getValueFromKey(
            CacheConstants.InfoBipCache.CACHE_NAME,
            CacheConstants.InfoBipCache.PIN_ID_KEY.concat(userId));

    if (StringUtils.isBlank(pinId)) {
      throw new BizException(ResponseMessage.OTP_EXPIRED);
    }

    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("pinId", pinId);
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer ".concat(getAccessToken()));
    InfoBipVerifyOtpResponse verifyOtpResponse =
        restUtil.sendPost(
            baseUrl.concat(INFO_BIP_VERIFY_OTP_API_URL),
            pathParams,
            null,
            headers,
            new InfoBipVerifyOtpRequest().pin(otp),
            ParameterizedTypeReference.forType(InfoBipVerifyOtpResponse.class));

    return verifyOtpResponse.getVerified();
  }

  private InfoBipLoginResponse login() {

    MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
    request.add(ConfigConstants.InfoBip.INFO_BIP_CLIENT_ID_KEY, username);
    request.add(ConfigConstants.InfoBip.INFO_BIP_CLIENT_SECRET_KEY, password);
    request.add(ConfigConstants.InfoBip.INFO_BIP_GRANT_TYPE_KEY, "client_credentials");

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");
    return restUtil.sendPost(
        baseUrl.concat(INFO_BIP_LOGIN_API_URL),
        headers,
        request,
        ParameterizedTypeReference.forType(InfoBipLoginResponse.class));
  }

  private String getAccessToken() {

    String tokenFromCache =
        cacheUtil.getValueFromKey(
            CacheConstants.InfoBipCache.CACHE_NAME, CacheConstants.InfoBipCache.INFOBIP_LOGIN_KEY);

    if (StringUtils.isNotBlank(tokenFromCache)
        && !jwtTokenUtils.isExtTokenExpired(tokenFromCache)) {
      return tokenFromCache;
    }
    String accessToken = login().getAccessToken();
    cacheUtil.addKey(
        CacheConstants.InfoBipCache.CACHE_NAME,
        CacheConstants.InfoBipCache.INFOBIP_LOGIN_KEY,
        accessToken);
    return accessToken;
  }
}
