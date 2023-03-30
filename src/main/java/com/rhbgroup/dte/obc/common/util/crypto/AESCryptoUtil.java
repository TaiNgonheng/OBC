package com.rhbgroup.dte.obc.common.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCryptoUtil extends CryptoUtil {

  private AESCryptoUtil() {}

  private static final String ALGORITHM = "AES/CBC/NoPadding";
  private static final String KEY_ALGORITHM = "AES";

  public static byte[] encrypt(String data, String secretKey, byte[] iv) {

    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
      return cipher.doFinal(pad(data.getBytes(StandardCharsets.UTF_8), cipher.getBlockSize()));

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] decrypt(byte[] data, String secretKey, byte[] iv) {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
      byte[] decryptedBytes = cipher.doFinal(data);

      return unPad(decryptedBytes);

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] getRandomIv(int blockSize) {
    SecureRandom random = new SecureRandom();
    byte[] iv = new byte[blockSize];
    random.nextBytes(iv);

    return iv;
  }
}
