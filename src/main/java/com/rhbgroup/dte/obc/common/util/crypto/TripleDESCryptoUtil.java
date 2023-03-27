package com.rhbgroup.dte.obc.common.util.crypto;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESCryptoUtil {

  private TripleDESCryptoUtil() {}

  private static final String ALGORITHM = "DESede/CBC/NoPadding";
  private static final String KEY_ALGORITHM = "DESede";

  public static byte[] encrypt(String data, String secretKey, String iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(
          Cipher.ENCRYPT_MODE,
          new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM),
          new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));

      return cipher.doFinal(
          CryptoPadding.pad(data.getBytes(StandardCharsets.UTF_8), cipher.getBlockSize()));

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] decrypt(byte[] data, String secretKey, String iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(
          Cipher.DECRYPT_MODE,
          new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM),
          new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
      byte[] encryptedBytes = cipher.doFinal(data);
      return CryptoPadding.unPad(encryptedBytes);

    } catch (Exception ex) {
      return new byte[0];
    }
  }
}
