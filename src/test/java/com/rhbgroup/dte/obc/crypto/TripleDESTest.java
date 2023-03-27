package com.rhbgroup.dte.obc.crypto;

import com.rhbgroup.dte.obc.common.util.crypto.TripleDESCryptoUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripleDESTest {

  private String secretKey;
  private String iv;

  @BeforeEach
  public void setUp() {
    // Initialize the secret key and initialization vector
    secretKey = "123456789012345678901234"; // 24 bytes
    iv = "00000000"; // 8 bytes
  }

  @Test
  void testEncryptDecrypt() {
    // Test encryption and decryption of a message
    String message = "Hello, world!";
    byte[] encrypted = TripleDESCryptoUtil.encrypt(message, secretKey, iv);
    byte[] decrypted = TripleDESCryptoUtil.decrypt(encrypted, secretKey, iv);
    Assertions.assertNotNull(decrypted);

    String decryptedMessage = new String(decrypted);
    Assertions.assertEquals(message, decryptedMessage);
  }

  @Test
  void testEncryptDecryptWithPadding() {
    // Test encryption and decryption of a message with padding
    String message = "This is a longer message.";
    byte[] encrypted = TripleDESCryptoUtil.encrypt(message, secretKey, iv);
    byte[] decrypted = TripleDESCryptoUtil.decrypt(encrypted, secretKey, iv);
    Assertions.assertNotNull(decrypted);

    String decryptedMessage = new String(decrypted);
    Assertions.assertEquals(message, decryptedMessage);
  }

  @Test
  void testDecryptInvalidData() {
    // Test decryption of invalid data
    byte[] encrypted = new byte[] {0x01, 0x02, 0x03};
    byte[] decrypted = TripleDESCryptoUtil.decrypt(encrypted, secretKey, iv);
    Assertions.assertEquals(0, decrypted.length);
  }
}
