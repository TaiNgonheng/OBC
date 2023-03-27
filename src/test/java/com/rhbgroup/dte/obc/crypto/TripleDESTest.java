package com.rhbgroup.dte.obc.crypto;

import com.rhbgroup.dte.obc.common.util.crypto.TripleDESCryptoUtil;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripleDESTest {

  private byte[] secretKey;
  private byte[] iv;

  private static final String KEY_HEX = "5497B691458FC1CD31A16116701F57F8";
  private static final String IV_HEX = "0000000000000000";

  @BeforeEach
  public void setUp() throws DecoderException {
    // Initialize the secret key and initialization vector
    secretKey = doMagic(KEY_HEX);
    iv = Hex.decodeHex(IV_HEX); // 8 bytes
  }

  private byte[] doMagic(String key) throws DecoderException {
    String first2Bytes = KEY_HEX.substring(0, 16);
    key += first2Bytes;
    return Hex.decodeHex(key);
  }

  @Test
  void testEncryptDecrypt() {
    // Test encryption and decryption of a message
    String message = "Hello, world!";
    byte[] encrypted =
        TripleDESCryptoUtil.encrypt(message.getBytes(StandardCharsets.UTF_8), secretKey, iv);
    byte[] decrypted = TripleDESCryptoUtil.decrypt(encrypted, secretKey, iv);
    Assertions.assertNotNull(decrypted);

    String decryptedMessage = new String(decrypted);
    Assertions.assertEquals(message, decryptedMessage);
  }

  @Test
  void testEncryptDecryptWithPadding() {
    // Test encryption and decryption of a message with padding
    String message = "This is a longer message.";
    byte[] encrypted =
        TripleDESCryptoUtil.encrypt(message.getBytes(StandardCharsets.UTF_8), secretKey, iv);
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
