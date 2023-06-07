package com.rhbgroup.dte.obc.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.rhbgroup.dte.obc.common.util.SpringRestUtil;
import com.rhbgroup.dte.obc.common.util.crypto.AESCryptoUtil;
import com.rhbgroup.dte.obc.model.CDRBGetHsmKeyResponse;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class AESCryptoUtilTest {
  private static final String aesIv = "esdWoBKBeDpHEPBt56qw4g==";
  private static final String encPassword = "sflsClYsuxash+qkDIJftkotgkzWmndPi5iR/atfzXQ=";
  private static final String aesKey = "1234567812345678";
  private static final String hsmZmkKey = "5497B691458FC1CD31A16116701F57F8";
  private static final String zpk = "CE2A8D0559D8D131E2A7D175B8B8953C";
  @Mock SpringRestUtil springRestUtil;
  private CDRBRestClient cdrbRestClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    cdrbRestClient = new CDRBRestClient(springRestUtil, null);
  }

  @Test
  void testEncryptAndDecrypt() {
    String data = "123456789ABCDEF!";
    String aesKey = "1234567812345678";
    byte[] iv = AESCryptoUtil.getRandomIv(16);

    byte[] encryptedData = AESCryptoUtil.encrypt(data, aesKey, iv);
    assertNotNull(encryptedData);

    byte[] decryptBytes = AESCryptoUtil.decrypt(encryptedData, aesKey, iv);

    System.out.println(
        "aesIv : " + new String(Base64.getEncoder().encode(iv), StandardCharsets.UTF_8));
    System.out.println(
        "encPassword : "
            + new String(Base64.getEncoder().encode(encryptedData), StandardCharsets.UTF_8));

    String decryptedData = new String(decryptBytes, StandardCharsets.UTF_8);
    assertNotNull(decryptedData);
    assertEquals(data, decryptedData);
  }

  @Test
  void testHSMEncryption() {
    CDRBGetHsmKeyResponse hsmKeyResponse = new CDRBGetHsmKeyResponse();
    hsmKeyResponse.setZpk(zpk);
    doReturn(hsmKeyResponse).when(springRestUtil).sendGet(any(), any(), any());
    ReflectionTestUtils.setField(cdrbRestClient, "encPassword", encPassword);
    ReflectionTestUtils.setField(cdrbRestClient, "aesIv", aesIv);
    ReflectionTestUtils.setField(cdrbRestClient, "aesKey", aesKey);
    ReflectionTestUtils.setField(cdrbRestClient, "baseUrl", "");
    ReflectionTestUtils.setField(cdrbRestClient, "hsmZmkKey", hsmZmkKey);
    String[] generateHSMEncryptedPwd = cdrbRestClient.generateHSMEncryptedPwd();
    System.out.println("encryptedPwd1 : " + generateHSMEncryptedPwd[0]);
    System.out.println("encryptedPwd2 : " + generateHSMEncryptedPwd[1]);
  }

  @Test
  void testEncryptWithInvalidKey() {
    String data = "This is a secret message.";
    String secretKey = "invalid key";
    byte[] iv = AESCryptoUtil.getRandomIv(16);

    byte[] encryptedData = AESCryptoUtil.encrypt(data, secretKey, iv);
    assertEquals(0, encryptedData.length);
  }

  @Test
  void testDecryptWithInvalidData() {
    String encryptedData = "invalid data";
    String secretKey = "1234567812345678";
    byte[] iv = AESCryptoUtil.getRandomIv(16);

    byte[] decryptedData =
        AESCryptoUtil.decrypt(encryptedData.getBytes(StandardCharsets.UTF_8), secretKey, iv);
    assertEquals(0, decryptedData.length);
  }

  @Test
  void testGetRandomIv() {
    int blockSize = 16;
    byte[] iv = AESCryptoUtil.getRandomIv(blockSize);
    assertNotNull(iv);
    assertEquals(blockSize, iv.length);
  }
}
