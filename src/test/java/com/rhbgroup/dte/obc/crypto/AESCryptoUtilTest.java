package com.rhbgroup.dte.obc.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rhbgroup.dte.obc.common.util.crypto.AESCryptoUtil;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AESCryptoUtilTest {

  @Test
  void testEncryptAndDecrypt() {
    String data = "power@ranger";
    String secretKey = "1234567812345678";
    byte[] iv = AESCryptoUtil.getRandomIv(16);

    byte[] encryptedData = AESCryptoUtil.encrypt(data, secretKey, iv);
    assertNotNull(encryptedData);

    byte[] decryptBytes = AESCryptoUtil.decrypt(encryptedData, secretKey, iv);

    String decryptedData = new String(decryptBytes, StandardCharsets.UTF_8);
    assertNotNull(decryptedData);
    assertEquals(data, decryptedData);
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
