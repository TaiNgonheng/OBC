package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.common.util.crypto.AESCryptoUtil;
import com.rhbgroup.dte.obc.common.util.crypto.TripleDESCryptoUtil;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.model.CDRBLoginRequest;
import com.rhbgroup.dte.obc.model.CDRBLoginResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CDRBRestClient {

  private static final String X_API_KEY = "NQrIN7HPBt141uX5yw2SZ4NigpagyHkZ8cG9b2rf";

  private final ConfigService configService;
  private final SpringRestUtil restUtil;

  private String username;
  private String encPassword;
  private String hsmZmkKey;
  private String secretKey;
  private String zmkIv;
  private String baseUrl;

  @PostConstruct
  private void initConfigValues() {
    ConfigService cdrbAccount = configService.loadJSONValue(ConfigConstants.CDRB_ACCOUNT);
    username = cdrbAccount.getStringValue(ConfigConstants.USERNAME);
    encPassword = cdrbAccount.getStringValue(ConfigConstants.PASSWORD);
    hsmZmkKey = cdrbAccount.getStringValue(ConfigConstants.HSM_ZMK_KEY);
    secretKey = cdrbAccount.getStringValue(ConfigConstants.SECRET);
    zmkIv = cdrbAccount.getStringValue(ConfigConstants.IV);

    baseUrl = configService.getByConfigKey(ConfigConstants.CDRB_URL_KEY, ConfigConstants.VALUE);
  }

  public CDRBLoginResponse login() {

    byte[] password =
        AESCryptoUtil.decrypt(encPassword.getBytes(StandardCharsets.UTF_8), secretKey, zmkIv.getBytes());
    byte[] decryptedHsmKey =
        AESCryptoUtil.decrypt(getHsmKey().getBytes(StandardCharsets.UTF_8), secretKey, hsmZmkKey.getBytes());
    String passwordEnc1 =
        new String(
            TripleDESCryptoUtil.encrypt(
                new String(password, StandardCharsets.UTF_8),
                new String(decryptedHsmKey, StandardCharsets.UTF_8),
                hsmZmkKey),
            StandardCharsets.UTF_8);

    String pathUrl = baseUrl.concat("/auth/channel/obc/login");

    CDRBLoginRequest loginRequest =
        new CDRBLoginRequest().username(username).encPwd1(passwordEnc1).encPwd2(passwordEnc1);
    CDRBLoginResponse loginResponse =
        restUtil.sendPost(
            pathUrl,
            buildHeader(),
            loginRequest,
            ParameterizedTypeReference.forType(CDRBLoginResponse.class));

    validateResponseStatus(loginResponse);

    return loginResponse;
  }

  private void validateResponseStatus(CDRBLoginResponse loginResponse) {
    // Implementation
  }

  private String getHsmKey() {
    String pathUrl = baseUrl.concat("/auth/hsm-key");
    return restUtil.sendGet(
        pathUrl, buildHeader(), ParameterizedTypeReference.forType(String.class));
  }

  private Map<String, String> buildHeader() {
    Map<String, String> header = new HashMap<>();
    header.put("x-api-key", X_API_KEY);
    header.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    return header;
  }
}
