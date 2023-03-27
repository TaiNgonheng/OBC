package com.rhbgroup.dte.obc.rest;

import com.rhbgroup.dte.obc.common.constants.services.ConfigConstants;
import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.common.util.crypto.AESCryptoUtil;
import com.rhbgroup.dte.obc.common.util.crypto.CryptoUtil;
import com.rhbgroup.dte.obc.common.util.crypto.TripleDESCryptoUtil;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.model.CDRBGetHsmKeyResponse;
import com.rhbgroup.dte.obc.model.CDRBLoginRequest;
import com.rhbgroup.dte.obc.model.CDRBLoginResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;
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
  private String aesKey;
  private String aesIv;

  private String zmkIv;
  private String baseUrl;
  private String hsmZmkKey;

  @PostConstruct
  private void initConfigValues() throws JSONException {
    baseUrl = configService.getByConfigKey(ConfigConstants.CDRB_URL, ConfigConstants.VALUE);

    List<ConfigEntity> cdrbConfigs = configService.findByServicePrefix("CDRB_");
    ConfigEntity cdrbAccount =
        configService.filterByServiceKey(cdrbConfigs, ConfigConstants.CDRB_ACCOUNT);
    JSONObject cdrbAccountJson = new JSONObject(cdrbAccount.getConfigValue());

    username = cdrbAccountJson.getString(ConfigConstants.USERNAME);
    encPassword = cdrbAccountJson.getString(ConfigConstants.PASSWORD);
    aesKey = cdrbAccountJson.getString(ConfigConstants.AES_KEY);
    aesIv = cdrbAccountJson.getString(ConfigConstants.AES_IV);

    hsmZmkKey =
        new JSONObject(
                configService
                    .filterByServiceKey(cdrbConfigs, ConfigConstants.CDRB_HSM_ZMK)
                    .getConfigValue())
            .getString(ConfigConstants.VALUE);

    zmkIv =
        new JSONObject(
                configService
                    .filterByServiceKey(cdrbConfigs, ConfigConstants.CDRB_HSM_IV)
                    .getConfigValue())
            .getString(ConfigConstants.VALUE);
  }

  public CDRBLoginResponse login() {

    // Decrypted stored password using AES
    byte[] passwordDecrypted =
        AESCryptoUtil.decrypt(
            Base64.getDecoder().decode(encPassword.getBytes(StandardCharsets.UTF_8)),
            aesKey,
            Base64.getDecoder().decode(aesIv.getBytes(StandardCharsets.UTF_8)));

    String passwordHex = CryptoUtil.encodeHexString(passwordDecrypted);

    // Split password
    String[] passwordSplits = splitPassword(passwordHex);

    // Decrypt zpk using hsmZmkKey
    String decryptedZpk = decryptZpk(getHsmKey(), hsmZmkKey);

    String passwordEnc1 =
        new String(
            TripleDESCryptoUtil.encrypt(
                CryptoUtil.decodeHex(passwordSplits[0]),
                CryptoUtil.decodeHex(decryptedZpk),
                CryptoUtil.decodeHex(zmkIv)),
            StandardCharsets.UTF_8);

    String passwordEnc2 =
        passwordSplits[1] != null
            ? new String(
                TripleDESCryptoUtil.encrypt(
                    CryptoUtil.decodeHex(passwordSplits[1]),
                    CryptoUtil.decodeHex(decryptedZpk),
                    CryptoUtil.decodeHex(zmkIv)),
                StandardCharsets.UTF_8)
            : null;

    String pathUrl = baseUrl.concat("/auth/channel/obc/login");

    CDRBLoginRequest loginRequest =
        new CDRBLoginRequest().username(username).encPwd1(passwordEnc1).encPwd2(passwordEnc2);

    CDRBLoginResponse loginResponse =
        restUtil.sendPost(
            pathUrl,
            buildHeader(),
            loginRequest,
            ParameterizedTypeReference.forType(CDRBLoginResponse.class));

    validateResponseStatus(loginResponse);

    return loginResponse;
  }

  private String decryptZpk(String hsmKey, String hsmZmkKey) {
    try {
      return CryptoUtil.encodeHexString(TripleDESCryptoUtil.decrypt(
          Hex.decodeHex(hsmKey),
          doMagic(hsmZmkKey),
          Hex.decodeHex(zmkIv)));
    } catch (DecoderException ex) {
      return null;
    }
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

  private void validateResponseStatus(CDRBLoginResponse loginResponse) {
    // Implementation
  }

  private String getHsmKey() {
    String pathUrl = baseUrl.concat("/auth/hsm-key");
    CDRBGetHsmKeyResponse hsmKeyResponse =
        restUtil.sendGet(
            pathUrl,
            buildHeader(),
            ParameterizedTypeReference.forType(CDRBGetHsmKeyResponse.class));

    return hsmKeyResponse.getZpk();
  }

  private Map<String, String> buildHeader() {
    Map<String, String> header = new HashMap<>();
    header.put("x-api-key", X_API_KEY);
    header.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    return header;
  }

  private byte[] doMagic(String key) throws DecoderException {
    String first2Bytes = key.substring(0, 16);
    key += first2Bytes;
    return Hex.decodeHex(key);
  }
}
