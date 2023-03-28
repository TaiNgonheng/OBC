package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.model.*;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class InfoBipRestClient {
  private final SpringRestUtil restUtil;
  private final ConfigService configService;
  private String infoBipBaseUrl;
  private String infoBipAppId;
  private String infoBipOtpMessageId;
  private String infoBipUserName;
  private String infoBipPassword;

  @PostConstruct
  private void loadConfiguration() {
    infoBipBaseUrl =
        configService.getByConfigKey(
            ConfigConstants.InfoBip.INFO_BIP_URL_KEY, ConfigConstants.VALUE, String.class);
    infoBipAppId =
        configService.getByConfigKey(
            ConfigConstants.InfoBip.INFO_BIP_APP_ID_KEY, ConfigConstants.VALUE, String.class);
    infoBipOtpMessageId =
        configService.getByConfigKey(
            ConfigConstants.InfoBip.INFO_BIP_OTP_MESSAGE_ID_KEY,
            ConfigConstants.VALUE,
            String.class);
  }

  public InfoBipLoginResponse login(MultiValueMap<String, String> request) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");
    return restUtil.sendPost(
            infoBipBaseUrl.concat(ConfigConstants.InfoBip.INFO_BIP_LOGIN_API_PATH),
            headers,
            request,
            ParameterizedTypeReference.forType(InfoBipLoginResponse.class));
  }

  public InfoBipSendOtpResponse sendOtp(String phone, String token) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer ".concat(token));
    return restUtil.sendPost(
        infoBipBaseUrl.concat(ConfigConstants.InfoBip.INFO_BIP_SEND_OTP_PATH),
        headers,
        new InfoBipSendOtpRequest()
            .applicationId(infoBipAppId)
            .messageId(infoBipOtpMessageId)
            .to(phone)
            .from(ConfigConstants.InfoBip.INFO_BIP_SENDER_NAME),
        ParameterizedTypeReference.forType(InfoBipSendOtpResponse.class));
  }

  public Boolean verifyOtp(String otp, String pinId, String token) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("pinId", pinId);
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer ".concat(token));
    InfoBipVerifyOtpResponse verifyOtpResponse =
        restUtil.sendPost(
            infoBipBaseUrl.concat(ConfigConstants.InfoBip.INFO_BIP_VERIFY_OTP_API_PATH),
            pathParams,
            null,
            headers,
            new InfoBipVerifyOtpRequest().pin(otp),
            ParameterizedTypeReference.forType(InfoBipVerifyOtpResponse.class));
    return verifyOtpResponse.getVerified();
  }
}
