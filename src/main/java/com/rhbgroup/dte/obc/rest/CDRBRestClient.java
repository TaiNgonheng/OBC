package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.CacheConstants;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.common.util.crypto.AESCryptoUtil;
import com.rhbgroup.dte.obc.common.util.crypto.CryptoUtil;
import com.rhbgroup.dte.obc.common.util.crypto.TripleDESCryptoUtil;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetHsmKeyResponse;
import com.rhbgroup.dte.obc.model.CDRBLoginRequest;
import com.rhbgroup.dte.obc.model.CDRBLoginResponse;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.cache.expiry.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CDRBRestClient {

  private static final String GET_HSM_KEY_URL = "/auth/hsm-key";
  private static final String AUTHENTICATION_URL = "/auth/channel/obc/login";
  private static final String GET_ACCOUNT_DETAIL =
      "/corebankingnonfinancialclient/bakong-link-casa/accounts";

  private final JwtTokenUtils jwtTokenUtils;
  private final SpringRestUtil restUtil;

  private final CacheUtil cacheUtil;

  @Value("${obc.cdrb.url}")
  protected String baseUrl;

  @Value("${obc.cdrb.xApiKey}")
  protected String xApiKey;

  @Value("${obc.cdrb.username}")
  protected String username;

  @Value("${obc.cdrb.password}")
  protected String encPassword;

  @Value("${obc.cdrb.aesKey}")
  protected String aesKey;

  @Value("${obc.cdrb.aesIv}")
  protected String aesIv;

  @Value("${obc.cdrb.hsmZmk}")
  protected String hsmZmkKey;

  @Value("${obc.cdrb.zmkIv}")
  protected String zmkIv;

  @PostConstruct
  private void initCache() {
    cacheUtil.createCache(CacheConstants.CDRBCache.CACHE_NAME, Duration.FIVE_MINUTES);
  }

  public CDRBGetAccountDetailResponse getAccountDetail(
      String authorization, CDRBGetAccountDetailRequest request) {

    String accessToken = getAccessToken(authorization);

    try {
      return restUtil.sendPost(
          baseUrl.concat(GET_ACCOUNT_DETAIL),
          buildHeader(accessToken),
          request,
          ParameterizedTypeReference.forType(CDRBGetAccountDetailResponse.class));

    } catch (BizException ex) {
      throw new BizException(ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS);
    }
  }

  private String login() {

    // Decrypted stored password using AES
    byte[] passwordDecrypted =
        AESCryptoUtil.decrypt(
            Base64.getDecoder().decode(encPassword.getBytes(StandardCharsets.UTF_8)),
            aesKey,
            Base64.getDecoder().decode(aesIv.getBytes(StandardCharsets.UTF_8)));

    String passwordStr = new String(passwordDecrypted, StandardCharsets.UTF_8);

    CDRBLoginRequest loginRequest =
        new CDRBLoginRequest().username(username).encPwd1(passwordStr).encPwd2(passwordStr);

    CDRBLoginResponse cdrbLoginResponse =
        restUtil.sendPost(
            baseUrl.concat(AUTHENTICATION_URL),
            buildHeader(null),
            loginRequest,
            ParameterizedTypeReference.forType(CDRBLoginResponse.class));

    return cdrbLoginResponse.getToken();
  }

  private String getAccessToken(String authorization) {

    String cdrbLoginKey =
        CacheConstants.CDRBCache.CDRB_LOGIN_KEY.concat(
            jwtTokenUtils.getUsernameFromJwtToken(jwtTokenUtils.extractJwt(authorization)));

    String tokenFromCache =
        cacheUtil.getValueFromKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey);

    if (StringUtils.isBlank(tokenFromCache)) {
      String newToken = login();
      cacheUtil.addKey(CacheConstants.CDRBCache.CACHE_NAME, cdrbLoginKey, newToken);
      return newToken;
    }

    return tokenFromCache;
  }

  private byte[] padRight(String passwordSplit) {
    StringBuilder hexString =
        new StringBuilder(
            CryptoUtil.encodeHexString(passwordSplit.getBytes(StandardCharsets.UTF_8)));

    int blockSize = passwordSplit.length() < 6 ? 6 : 32;
    while (hexString.length() < blockSize) {
      hexString.append("F");
    }

    return CryptoUtil.decodeHex(hexString.toString());
  }

  private String[] constructPasswords(String passwordStr) {

    // Split password
    String[] passwordSplits = splitPassword(passwordStr);

    // Decrypt zpk using hsmZmkKey
    String decryptedZpk = decryptZpk(getHsmKey(), hsmZmkKey);

    String passwordEnc1 =
        CryptoUtil.encodeHexString(
                TripleDESCryptoUtil.encrypt(
                    padRight(passwordSplits[0]),
                    addKeyPadding(decryptedZpk),
                    CryptoUtil.decodeHex(zmkIv)))
            .toUpperCase();

    String passwordEnc2 =
        passwordSplits[1] != null
            ? CryptoUtil.encodeHexString(
                    TripleDESCryptoUtil.encrypt(
                        padRight(passwordSplits[1]),
                        addKeyPadding(decryptedZpk),
                        CryptoUtil.decodeHex(zmkIv)))
                .toUpperCase()
            : passwordEnc1;

    return new String[] {passwordEnc1, passwordEnc2};
  }

  private String decryptZpk(String hsmKey, String hsmZmkKey) {
    return CryptoUtil.encodeHexString(
        TripleDESCryptoUtil.decrypt(
            CryptoUtil.decodeHex(hsmKey), addKeyPadding(hsmZmkKey), CryptoUtil.decodeHex(zmkIv)));
  }

  private String[] splitPassword(String password) {
    String[] passwordSplit = new String[2];
    int passLength = password.length();

    if (passLength <= 12) {
      passwordSplit[0] = password;
      return passwordSplit;
    }

    int halfLength = passLength / 2;
    String pass1 = password.substring(0, halfLength);
    String pass2 = password.substring(halfLength, passLength);
    passwordSplit[0] = pass1;
    passwordSplit[1] = pass2;

    return passwordSplit;
  }

  private String getHsmKey() {

    CDRBGetHsmKeyResponse hsmKeyResponse =
        restUtil.sendGet(
            baseUrl.concat(GET_HSM_KEY_URL),
            buildHeader(null),
            ParameterizedTypeReference.forType(CDRBGetHsmKeyResponse.class));

    return hsmKeyResponse.getZpk();
  }

  private Map<String, String> buildHeader(String accessToken) {

    Map<String, String> header = new HashMap<>();
    header.put("x-api-key", xApiKey);
    header.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    if (StringUtils.isNotBlank(accessToken)) {
      header.put("Authorization", "Bearer ".concat(accessToken));
    }

    return header;
  }

  private byte[] addKeyPadding(String key) {
    String first2Bytes = key.substring(0, 16);
    key += first2Bytes;
    return CryptoUtil.decodeHex(key);
  }
}
